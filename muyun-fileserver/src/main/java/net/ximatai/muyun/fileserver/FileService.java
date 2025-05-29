package net.ximatai.muyun.fileserver;

import io.vertx.core.Vertx;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.exception.FileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.UUID;

@ApplicationScoped
public class FileService implements IFileService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    FileServerConfig config;

    @Inject
    Vertx vertx;

    private String folderPath;

    @PostConstruct
    public void init() {
        // 检查配置的文件夹是否存在
        folderPath = config.uploadPath();
        if (!folderPath.endsWith("/") && !folderPath.endsWith("\\")) {
            folderPath = folderPath + "/";
        }
        Path path = Paths.get(folderPath);
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                logger.info("文件夹已创建: {}", folderPath);
            } else {
                logger.info("文件夹已存在: {}", folderPath);
            }
        } catch (Exception e) {
            logger.error("创建文件夹失败", e);
        }
    }

    // 异步得到文件信息
    public FileInfoEntity info(String id) throws FileException {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }

        Path pathN = Paths.get(folderPath + suffixFileNameWithN(id));
        Path pathO = Paths.get(folderPath + suffixFileNameWithO(id));

        if (!Files.isReadable(pathN) || !Files.isReadable(pathO)) {
            throw new FileException("File not found: " + id);
        }

        File fileContent = pathO.toFile();
        String finalId = id;

        try (BufferedReader br = new BufferedReader(new FileReader(pathN.toFile()))) {
            String line = br.readLine();
            String suffix = line.split("\\.")[1];
            String createTime = "00:00";
            try {
                BasicFileAttributes attrs = Files.readAttributes(pathO, BasicFileAttributes.class);
                FileTime creationTime = attrs.creationTime();
                createTime = creationTime.toString();
            } catch (IOException e) {
                logger.error("Failed to read file attributes", e);
            }
            return new FileInfoEntity(line, fileContent.length(), suffix, finalId, createTime);
        } catch (IOException e) {
            throw new FileException(e);
        }

    }

    public String save(File file) {
        return this.save(file, file.getName());
    }

    // 同步save方法
    public String save(File file, String assignName) {
        String saveId = generateBsyUid();
        String saveFileNameUid = suffixFileNameWithN(saveId);
        String saveFileContextUid = suffixFileNameWithO(saveId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(folderPath + saveFileNameUid))) {
            writer.write(assignName);
        } catch (IOException e) {
            logger.error("Failed to write file name", e);
        }
        Path sourcePath = Paths.get(file.getAbsolutePath());
        Path targetPath = Paths.get(folderPath + saveFileContextUid);
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to move file", e);
        }
        return "%s@%s".formatted(saveId, assignName);
    }

    // 获取文件
    public File get(String idOrName) throws FileException {
        // 根据文件名查找文件
        File fileDirectory = new File(folderPath);
        File[] files = fileDirectory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.getName().equals(idOrName)) {
                    return file;
                }
            }
        }

        // 根据id查找文件
        String id = idOrName;
        if (idOrName.contains("@")) {
            id = id.split("@")[0];
        }

        Path pathN = Paths.get(folderPath + suffixFileNameWithN(id));
        Path pathO = Paths.get(folderPath + suffixFileNameWithO(id));

        if (!Files.isReadable(pathN) || !Files.isReadable(pathO)) {
            throw new FileException("File not found: " + idOrName);
        }

        try {
            String name = Files.readString(pathN, StandardCharsets.UTF_8);

            //准备临时文件
            File tempFile;
            if (name.contains(".")) {
                tempFile = File.createTempFile("fileserver", "." + name.split("\\.")[1]);
            } else {
                tempFile = File.createTempFile("fileserver", ".tmp");
            }

            tempFile.deleteOnExit();

            Files.copy(pathO, tempFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

            return tempFile;
        } catch (IOException e) {
            throw new FileException(e);
        }
    }

    // 丢弃服务器端中的文件
    public boolean delete(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }

        String deleteNamePath = suffixFileNameWithN(folderPath + id);
        String deleteContentPath = suffixFileNameWithO(folderPath + id);
        File fileN = new File(deleteNamePath);
        File fileO = new File(deleteContentPath);
        vertx.fileSystem().delete(deleteNamePath, res -> {
            if (res.succeeded()) {
                logger.info("FileName deleted successfully: {}", deleteNamePath);
            } else {
                logger.error("Failed to delete file: {}", deleteNamePath, res.cause());
            }
        });
        vertx.fileSystem().delete(deleteContentPath, res -> {
            if (res.succeeded()) {
                logger.info("FileContent deleted successfully: {}", deleteContentPath);
            } else {
                logger.error("Failed to delete file: {}", deleteContentPath, res.cause());
            }
        });
        return fileN.exists() || fileO.exists();
    }

    private String generateBsyUid() {
        UUID uid = UUID.randomUUID();
        return "bsy-" + uid;
    }
}
