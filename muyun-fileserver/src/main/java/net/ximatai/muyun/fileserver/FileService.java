package net.ximatai.muyun.fileserver;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.FileSystem;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

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
                System.out.println("文件夹已创建: " + folderPath);
            } else {
                System.out.println("文件夹已存在: " + folderPath);
            }
        } catch (Exception e) {
            logger.error("创建文件夹失败", e);
        }
    }

    // 异步得到文件信息
    public Future<FileInfoEntity> asyncInfo(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        Promise<FileInfoEntity> promise = Promise.promise();
        String fileNamePath = suffixFileNameWithN(folderPath + id);
        String fileContentPath = suffixFileNameWithO(folderPath + id);
        File fileContent = new File(fileContentPath);
        String finalId = id;
        vertx.fileSystem().readFile(fileNamePath, result -> {
            if (result.succeeded()) {
                Buffer buffer = result.result();
                String line = buffer.toString("UTF-8");
                String suffix = line.split("\\.")[1];
                Path path = Paths.get(fileNamePath);
                String createTime = "00:00";
                try {
                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
                    FileTime creationTime = attrs.creationTime();
                    createTime = creationTime.toString();
                } catch (IOException e) {
                    logger.error("Failed to read file attributes", e);
                }
                FileInfoEntity entity = new FileInfoEntity(line, fileContent.length(), suffix, finalId, createTime);
                promise.complete(entity);
            } else {
                logger.error("Failed to read file name: " + result.cause());
                promise.fail(result.cause());
            }
        });
        return promise.future();
    }

    public FileInfoEntity info(String id) {
        CompletableFuture<FileInfoEntity> completableFuture = new CompletableFuture<FileInfoEntity>();
        asyncInfo(id)
            .onSuccess(completableFuture::complete)
            .onFailure(completableFuture::completeExceptionally);
        try {
            return completableFuture.get();
        } catch (InterruptedException | ExecutionException e) {
            logger.error("Failed to get file info", e);
            throw new RuntimeException(e);
        }
    }

    public String save(File file) {
        return this.save(file, file.getName());
    }

    // 异步save方法
    public String save1(File file, String assignName) {
        String saveId = generateBsyUid();
        String saveFileNameUid = suffixFileNameWithN(saveId);
        String saveFileContextUid = suffixFileNameWithO(saveId);
        FileSystem fileSystem = vertx.fileSystem();
        // 写入文件名
        fileSystem.writeFile(folderPath + saveFileNameUid, Buffer.buffer(assignName));
        fileSystem.copy(file.getAbsolutePath(), folderPath + saveFileContextUid);
        fileSystem.delete(file.getAbsolutePath());
        return "%s@%s".formatted(saveId, assignName);
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
    public File get(String idOrName) {
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
        String nameFile = suffixFileNameWithN(id);
        String contentFile = suffixFileNameWithO(id);
        String nameFilePath = folderPath + nameFile;
        String contentFilePath = folderPath + contentFile;
        Path pathN = Paths.get(nameFilePath);
        Path pathO = Paths.get(contentFilePath);
        if (!Files.exists(pathN)) return null;
        try {
            String name = Files.readString(pathN, StandardCharsets.UTF_8);
            byte[] bytes = Files.readAllBytes(pathO);
            // 上传文件副本
            File newFile = File.createTempFile(name.split("\\.")[0], "." + name.split("\\.")[1]);
            Files.write(Paths.get(newFile.getPath()), bytes);
            String indirectPath = newFile.getAbsolutePath().split(name.split("\\.")[0])[0];
            String uid = generateBsyUid();
            String directoryPath = indirectPath + uid;
            File directory = new File(directoryPath);
            boolean result = directory.mkdirs();
            Path sourcePath = Paths.get(newFile.getAbsolutePath());
            Path targetDir = Paths.get(directory.getAbsolutePath());
            Path targetPath = targetDir.resolve(name);
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            return targetPath.toFile();
        } catch (IOException e) {
            throw new RuntimeException(e);
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
