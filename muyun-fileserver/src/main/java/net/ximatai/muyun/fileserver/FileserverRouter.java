package net.ximatai.muyun.fileserver;

import io.quarkus.vertx.web.Route;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.FileUpload;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.vertx.core.Vertx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;

public class FileserverRouter {

    final Logger logger = LoggerFactory.getLogger(getClass());

    String originalFileName;

    @Inject
    FileserverConfig config;

    @Inject
    Vertx vertx;
    
    void init(@Observes Router router, Vertx vertx){
        router.get(config.pagePath() + "index").handler(this::indexFunc);
        router.post(config.pagePath() + "form").handler(this::form);
        router.get(config.pagePath() + "download/:fileUid").handler(this::download);
        router.get(config.pagePath() + "delete/:uid").handler(this::delete);
        router.get(config.pagePath() + "info/:uid").handler(this::info);
    }

    // @Route(path = "/fileServer/index", methods = Route.HttpMethod.GET)
    public void indexFunc(RoutingContext ctx) {
        ctx.response()
            .putHeader("content-type", "text/html")
            .end(
                // "<form action=\"/fileServer/form\" method=\"post\" enctype=\"multipart/form-data\">\n"
                "<form action=\"form\" method=\"post\" enctype=\"multipart/form-data\">\n"
                    + "    <div>\n"
                    + "        <label for=\"name\">Select a file:</label>\n"
                    + "        <input type=\"file\" name=\"file\" />\n"
                    + "    </div>\n"
                    + "    <div class=\"button\">\n"
                    + "        <button type=\"submit\">Send</button>\n"
                    + "    </div>"
                    + "</form>"
            );
    }

    // @Route(path = "/fileServer/form", methods = Route.HttpMethod.POST)
    public void form(RoutingContext ctx) {

        // 支持分块传输编码
        ctx.response().setChunked(true);
        for (FileUpload f : ctx.fileUploads()) {
            // 获取文件名、文件大小、uid
            String uploadedFileName = f.uploadedFileName();
            originalFileName = f.fileName();
            long fileSize = f.size();
            String uid = "bsy-" + uploadedFileName.split("\\\\")[2];
            String fileNameUid = suffixFileNameWithN(uid);
            String fileContextUid = suffixFileNameWithO(uid);
            vertx.fileSystem().writeFile(config.uploadPath() + fileNameUid, Buffer.buffer(originalFileName));
            vertx.fileSystem().copy(uploadedFileName, config.uploadPath() + fileContextUid);
            vertx.fileSystem().delete(uploadedFileName);
            ctx.response().write(uid);
        }
        ctx.response().end();
    }

    // @Route(path = "/fileServer/download/:fileUid", methods = Route.HttpMethod.GET)
    public void download(RoutingContext ctx) {
        String fileUid = ctx.pathParam("fileUid");
        String nameFile = suffixFileNameWithN(fileUid);
        String contentFile = suffixFileNameWithO(fileUid);
        String contentFilePath = config.uploadPath() + contentFile;
        File file = new File(contentFilePath);
        String nameFilePath = config.uploadPath() + nameFile;
        vertx.fileSystem().readFile(nameFilePath, result -> {
            if (result.succeeded()) {
                Buffer buffer = result.result();
                String content = buffer.toString("UTF-8");
                if (file.exists()) {
                    ctx.response()
                        .putHeader("Content-Disposition", "attachment; filename=" + content)
                        .sendFile(file.getPath());
                }
            } else {
                logger.error("Failed to read file name: " + result.cause());
                ctx.fail(result.cause());
            }
        });
    }

    // @Route(path = "/fileServer/delete/:uid", methods = Route.HttpMethod.GET)
    public void delete(RoutingContext ctx) {
        String uid = ctx.pathParam("uid");
        String deleteNamePath = suffixFileNameWithN(config.uploadPath() + uid);
        String deleteContentPath = suffixFileNameWithO(config.uploadPath() + uid);
        File fileN = new File(deleteNamePath);
        File fileO = new File(deleteContentPath);
        boolean isDelete1 = fileN.delete();
        boolean isDelete2 = fileO.delete();
        ctx.response().end("Successfully deleted.");
    }

    // @Route(path = "/fileServer/info/:uid", methods = Route.HttpMethod.GET)
    public void info(RoutingContext ctx) {
        String uid = ctx.pathParam("uid");
        String fileNamePath = suffixFileNameWithN(config.uploadPath() + uid);
        String fileContentPath = suffixFileNameWithO(config.uploadPath() + uid);
        File file = new File(fileNamePath);
        File fileContent = new File(fileContentPath);
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

                JsonObject jsonObject = new JsonObject()
                    .put("name", line)
                    .put("size", fileContent.length())
                    .put("suffix", suffix)
                    .put("uid", uid)
                    .put("time", createTime);
                ctx.response()
                    .putHeader("Content-Type", "application/json")
                    .end(jsonObject.toString());

            } else {
                logger.error("Failed to read file name: " + result.cause());
                ctx.fail(result.cause());
            }
        });
    }

    // uid文件名处理方法
    private String suffixFileNameWithN(String fileName) {
        return fileName + "-n";
    }

    private String suffixFileNameWithO(String fileName) {
        return fileName + "-o";
    }
}
