package net.ximatai.muyun.fileserver;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@ApplicationScoped
public class FileService implements IFileService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    String originalFileName;

    @Inject
    FileServerConfig config;

    @Inject
    Vertx vertx;

    private String getRootPath() {
        String rootPath = config.pagePath();
        if (!rootPath.startsWith("/")) {
            rootPath = "/" + rootPath;
        }
        if (!rootPath.endsWith("/")) {
            rootPath = rootPath + "/";
        }
        return rootPath;
    }

    void init(@Observes Router router, Vertx vertx) {
        router.get(getRootPath() + "index").handler(this::indexFunc);
        router.post(getRootPath() + "upload").handler(this::upload);
        router.get(getRootPath() + "download/:id").handler(this::download);
        router.get(getRootPath() + "delete/:id").handler(this::delete);
        router.get(getRootPath() + "info/:id").handler(this::info);
    }

    // @Route(path = "/fileServer/index", methods = Route.HttpMethod.GET)
    private void indexFunc(RoutingContext ctx) {
        ctx.response()
            .putHeader("content-type", "text/html")
            .end(
                """
                            <form action="upload" method="post" enctype="multipart/form-data">
                                 <div>
                                    <label for="name">Select a file:</label>
                                    <input type="file" name="file" />
                                </div>
                                <div class="button">
                                    <button type="submit">Send</button>
                                </div>
                            </form>
                    """
            );
    }

    // @Route(path = "/fileServer/form", methods = Route.HttpMethod.POST)
    private void upload(RoutingContext ctx) {
        // 支持分块传输编码
        ctx.response().setChunked(true);
        for (FileUpload f : ctx.fileUploads()) {
            String uploadedFileName = f.uploadedFileName();
            originalFileName = f.fileName();
            File file = new File(uploadedFileName);
            String id = save(file, originalFileName);
            ctx.response().write(id);
        }
        ctx.response().end();
    }

    // @Route(path = "/fileServer/download/:id", methods = Route.HttpMethod.GET)
    private void download(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        File fileObtained = get(id);
        // 发送文件到客户端
        String nameFile = suffixFileNameWithN(id);
        String nameFilePath = config.uploadPath() + nameFile;
        vertx.fileSystem().readFile(nameFilePath, result -> {
            if (result.succeeded()) {
                Buffer buffer = result.result();
                String content = buffer.toString("UTF-8");
                if (fileObtained.exists()) {
                    ctx.response()
                        .putHeader("Content-Disposition", "attachment; filename=" + content)
                        .sendFile(fileObtained.getPath());
                    vertx.fileSystem().delete(fileObtained.getPath());
                }
            } else {
                logger.error("Failed to read file name: " + result.cause());
                ctx.fail(result.cause());
            }
        });
    }

    // @Route(path = "/fileServer/delete/:id", methods = Route.HttpMethod.GET)
    private void delete(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        boolean isDeleted = delete(id);
        if (isDeleted) {
            ctx.response().end("Successfully deleted.");
        } else {
            ctx.response().end("Failed to delete.");
        }
    }

    private void info(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        asyncInfo(id)
            .onSuccess(entity -> {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(entity.toJson().toString());

            }).onFailure(err -> {
                logger.error("Failed to get file info: " + err);
                ctx.fail(err);
            });
    }

    // 异步得到文件信息
    public Future<FileInfoEntity> asyncInfo(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        Promise<FileInfoEntity> promise = Promise.promise();
        String fileNamePath = suffixFileNameWithN(config.uploadPath() + id);
        String fileContentPath = suffixFileNameWithO(config.uploadPath() + id);
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
    public String save(File file, String assignName) {
        String saveId = generateBsyUid();
        String saveFileNameUid = suffixFileNameWithN(saveId);
        String saveFileContextUid = suffixFileNameWithO(saveId);
        // 写入文件名
        vertx.fileSystem().writeFile(config.uploadPath() + saveFileNameUid, Buffer.buffer(assignName));
        vertx.fileSystem().copy(file.getAbsolutePath(), config.uploadPath() + saveFileContextUid);
        vertx.fileSystem().delete(file.getAbsolutePath());
        return "%s@%s".formatted(saveId, assignName);
    }

    // 获取文件
    public File get(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }

        String nameFile = suffixFileNameWithN(id);
        String contentFile = suffixFileNameWithO(id);
        String nameFilePath = config.uploadPath() + nameFile;
        String contentFilePath = config.uploadPath() + contentFile;
        Path pathN = Paths.get(nameFilePath);
        Path pathO = Paths.get(contentFilePath);
        if (!Files.exists(pathN)) return null;
        try {
            String name = Files.readString(pathN);
            String context = Files.readString(pathO);
            String fileBak = config.uploadPath() + name;
            File newFile = new File(fileBak);
            if (newFile.createNewFile()) {
                Files.writeString(Paths.get(fileBak), context);
                return newFile;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    // 丢弃服务器端中的文件
    public boolean delete(String id) {
        if (id.contains("@")) {
            id = id.split("@")[0];
        }

        String deleteNamePath = suffixFileNameWithN(config.uploadPath() + id);
        String deleteContentPath = suffixFileNameWithO(config.uploadPath() + id);
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

    // uid文件名处理方法
    private String suffixFileNameWithN(String fileName) {
        return fileName + "-n";
    }

    private String suffixFileNameWithO(String fileName) {
        return fileName + "-o";
    }

    private String generateBsyUid() {
        UUID uid = UUID.randomUUID();
        return "bsy-" + uid.toString();
    }
}
