package net.ximatai.muyun.fileserver;

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

@ApplicationScoped
public class FileServer {
    
    final Logger logger = LoggerFactory.getLogger(getClass());

    String originalFileName;

    @Inject
    FileServerConfig config;

    @Inject
    Vertx vertx;
    
    @Inject
    IFileService fileService;

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

    private String getUploadPath() {
        String uploadPath = config.uploadPath();
        if (!uploadPath.endsWith("/")) {
            uploadPath = uploadPath + "/";
        }
        return uploadPath;
    }

    void init(@Observes Router router) {
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
            String id = fileService.save(file, originalFileName);
            ctx.response()
                // .putHeader("Content-Type", "text/plain;charset=utf-8")
                .write(id);
        }
        ctx.response()
            // .putHeader("Content-Type", "text/plain;charset=utf-8")
            .end();
    }

    // @Route(path = "/fileServer/download/:id", methods = Route.HttpMethod.GET)
    private void download(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        if (id.contains("@")) {
            id = id.split("@")[0];
        }
        File fileObtained = fileService.get(id);
        // 发送文件到客户端
        String nameFile = fileService.suffixFileNameWithN(id);
        String nameFilePath = getUploadPath() + nameFile;
        vertx.fileSystem().readFile(nameFilePath, result -> {
            if (result.succeeded()) {
                Buffer buffer = result.result();
                String content = buffer.toString("UTF-8");
                if (fileObtained.exists()) {
                    ctx.response()
                        .putHeader("Content-Disposition", "attachment; filename=" + content)
                        .sendFile(fileObtained.getPath());
                    // vertx.fileSystem().delete(fileObtained.getPath());
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
        boolean isDeleted = fileService.delete(id);
        if (isDeleted) {
            ctx.response().end("Successfully deleted.");
        } else {
            ctx.response().end("Failed to delete.");
        }
    }

    private void info(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        fileService.asyncInfo(id)
            .onSuccess(entity -> {
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(entity.toJson().toString());
            }).onFailure(err -> {
                logger.error("Failed to get file info: " + err);
                ctx.fail(err);
            });
    }
    
}
