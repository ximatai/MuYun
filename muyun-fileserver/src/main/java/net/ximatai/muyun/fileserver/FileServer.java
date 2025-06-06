package net.ximatai.muyun.fileserver;

import io.vertx.core.Vertx;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.exception.FileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@ApplicationScoped
public class FileServer {

    final Logger logger = LoggerFactory.getLogger(getClass());

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
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
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
                            <!DOCTYPE html>
                            <html lang="en">
                            <head>
                                <meta charset="UTF-8">
                                <title>Title</title>
                            </head>
                            <body>
                            <form enctype="multipart/form-data" action="/fileServer/upload" method="post">
                                <input name="files1"  type="file" multiple />
                                <button type="submit">提交</button>
                            </form>
                            </body>
                            </html>
                    """
            );
    }

    // @Route(path = "/fileServer/form", methods = Route.HttpMethod.POST)
    private void upload(RoutingContext ctx) {
        // 支持分块传输编码
        ctx.response().setChunked(true);
        List<String> ids = new ArrayList<>();
        for (FileUpload f : ctx.fileUploads()) {
            String uploadedFileName = f.uploadedFileName();
            String originalFileName = f.fileName();
            logger.info("uploaded file name: {}", uploadedFileName);
            File file = new File(uploadedFileName);
            ids.add(fileService.save(file, originalFileName));
        }

        ctx.response()
            .putHeader("Content-Type", "text/plain;charset=utf-8")
            .end(String.join("\n", ids));
    }

    // @Route(path = "/fileServer/download/:id", methods = Route.HttpMethod.GET)
    private void download(RoutingContext ctx) {
        String id = ctx.pathParam("id");
        try {
            File fileObtained = fileService.get(id);
            String name = fileObtained.getName();

            if (id.contains("@") && id.length() > 35) {
                name = fileService.info(id).name;
            }

            ctx.response()
                .putHeader("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8))
                .putHeader("Content-type", "application/octet-stream")
                .sendFile(fileObtained.getPath());
        } catch (FileException e) {
            ctx.fail(e);
        }

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

        try {
            ctx.response()
                .putHeader("Content-Type", "application/json")
                .end(fileService.info(id).toJson().encode());
        } catch (FileException e) {
            ctx.fail(e);
        }

    }

}
    
