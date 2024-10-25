package net.ximatai.muyun.fileserver;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    private String getUploadPath() {
        String uploadPath = config.uploadPath();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }
        return uploadPath;
    }

    // 异步得到文件信息
    public Future<FileInfoEntity> asyncInfo(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        Promise<FileInfoEntity> promise = Promise.promise();
        String fileNamePath = suffixFileNameWithN(getUploadPath() + id);
        String fileContentPath = suffixFileNameWithO(getUploadPath() + id);
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
            .onSuccess(entity -> {
                completableFuture.complete(entity);
            })
            .onFailure(err -> {
                completableFuture.completeExceptionally(err);
            });
        try {
            return completableFuture.get();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public String save(File file) {
        return this.save(file, file.getName());
    }

    // 保存文件
//    public String save(File file, String assignName) {
//        String saveId = generateBsyUid();
//        String saveFileNameUid = suffixFileNameWithN(saveId);
//        String saveFileContextUid = suffixFileNameWithO(saveId);
//        FileSystem fileSystem = vertx.fileSystem();
//        // 写入文件名
//        fileSystem.writeFile(getUploadPath() + saveFileNameUid, Buffer.buffer(assignName));
//        fileSystem.copy(file.getAbsolutePath(), getUploadPath() + saveFileContextUid);
//        fileSystem.delete(file.getAbsolutePath());
//        return "%s@%s".formatted(saveId, assignName);
//    }

    // 同步save方法
    public String save(File file, String assignName) {
        String saveId = generateBsyUid();
        String saveFileNameUid = suffixFileNameWithN(saveId);
        String saveFileContextUid = suffixFileNameWithO(saveId);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(getUploadPath() + saveFileNameUid))) {
            writer.write(assignName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Path sourcePath = Paths.get(file.getAbsolutePath());
        Path targetPath = Paths.get(getUploadPath() + saveFileContextUid);
        try {
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "%s@%s".formatted(saveId, assignName);
    }

    // 获取文件
    public File get(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        String nameFile = suffixFileNameWithN(id);
        String contentFile = suffixFileNameWithO(id);
        String nameFilePath = getUploadPath() + nameFile;
        String contentFilePath = getUploadPath() + contentFile;
        Path pathN = Paths.get(nameFilePath);
        Path pathO = Paths.get(contentFilePath);
        if (!Files.exists(pathN)) return null;
        try {
            String name = Files.readString(pathN);
            byte[] bytes = Files.readAllBytes(pathO);
            // 上传文件副本
            File newFile = File.createTempFile(name.split("\\.")[0], "." + name.split("\\.")[1]);
            Files.write(Paths.get(newFile.getPath()), bytes);
            return newFile;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // return null;
    }

    // 丢弃服务器端中的文件
    public boolean delete(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }

        String deleteNamePath = suffixFileNameWithN(getUploadPath() + id);
        String deleteContentPath = suffixFileNameWithO(getUploadPath() + id);
        File fileN = new File(deleteNamePath);
        File fileO = new File(deleteContentPath);
        vertx.fileSystem().delete(deleteNamePath, res -> {
            if (res.succeeded()) {
                logger.info("FileName deleted successfully: " + deleteNamePath);
            } else {
                logger.error("Failed to delete file: " + deleteNamePath, res.cause());
            }
        });
        vertx.fileSystem().delete(deleteContentPath, res -> {
            if (res.succeeded()) {
                logger.info("FileContent deleted successfully: " + deleteContentPath);
            } else {
                logger.error("Failed to delete file: " + deleteContentPath, res.cause());
            }
        });
        return fileN.exists() || fileO.exists();
    }

    private String generateBsyUid() {
        UUID uid = UUID.randomUUID();
        return "bsy-" + uid;
    }
}
