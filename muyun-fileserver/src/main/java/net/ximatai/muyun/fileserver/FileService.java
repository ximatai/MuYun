package net.ximatai.muyun.fileserver;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
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

//TODO 抽象一个 IFileService 接口，对Java层暴露相关方法
@ApplicationScoped
public class FileService {

    final Logger logger = LoggerFactory.getLogger(getClass());

    String originalFileName;

    @Inject
    FileServerConfig config;

    @Inject
    Vertx vertx;

    // TODO 代码不够优雅，尤其是最后一行 return
    private String getRootPath() {
        String rootPath = config.pagePath();
        rootPath = rootPath.startsWith("/") ? rootPath : "/" + rootPath;
        return rootPath.endsWith("/") ? rootPath : rootPath + "/";
    }

    void init(@Observes Router router, Vertx vertx) {
        router.get(getRootPath() + "index").handler(this::indexFunc);
        router.post(getRootPath() + "form").handler(this::form);
        router.get(getRootPath() + "download/:fileUid").handler(this::download);
        router.get(getRootPath() + "delete/:uid").handler(this::delete);
        router.get(getRootPath() + "info/:uid").handler(this::info);
    }

    //TODO 改成 """ """ 的方式存放字符串
    // @Route(path = "/fileServer/index", methods = Route.HttpMethod.GET)
    private void indexFunc(RoutingContext ctx) {
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

    // TODO 要知道这是对 前端开放的上传接口，所以叫 form 不合适，正常应该叫 upload
    // @Route(path = "/fileServer/form", methods = Route.HttpMethod.POST)
    private void form(RoutingContext ctx) {

        // 支持分块传输编码
        ctx.response().setChunked(true);
        for (FileUpload f : ctx.fileUploads()) {
            // 原来之前面向http的处理逻辑
            /*
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
            */

            // 面向Java的文件上传逻辑
            String uploadedFileName = f.uploadedFileName();
            originalFileName = f.fileName();
            File file = new File(uploadedFileName);
            String uid = save(file);
            ctx.response().write(uid);
        }
        ctx.response().end();
    }

    // @Route(path = "/fileServer/download/:fileUid", methods = Route.HttpMethod.GET)
    private void download(RoutingContext ctx) {
        String fileUid = ctx.pathParam("fileUid");
        File fileObtained = obtain(fileUid);
        // 发送文件到客户端
        String nameFile = suffixFileNameWithN(fileUid);
        String nameFilePath = config.uploadPath() + nameFile;
        vertx.fileSystem().readFile(nameFilePath, result -> {
            if (result.succeeded()) {
                Buffer buffer = result.result();
                String content = buffer.toString("UTF-8");
                if (fileObtained.exists()) {
                    ctx.response()
                        .putHeader("Content-Disposition", "attachment; filename=" + content)
                        .sendFile(fileObtained.getPath());
                }
            } else {
                logger.error("Failed to read file name: " + result.cause());
                ctx.fail(result.cause());
            }
        });

    }

    // @Route(path = "/fileServer/delete/:uid", methods = Route.HttpMethod.GET)
    private void delete(RoutingContext ctx) {
        String uid = ctx.pathParam("uid");
        boolean isDeleted = drop(uid);
        if (isDeleted) {
            ctx.response().end("Successfully deleted.");
        } else {
            ctx.response().end("Failed to delete.");
        }
    }

    // @Route(path = "/fileServer/info/:uid", methods = Route.HttpMethod.GET)
    private void info(RoutingContext ctx) {
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

//        try {
//            FileInfoEntity fileInfoEntity = show(uid);
//            String name = fileInfoEntity.getName();
//            long size = fileInfoEntity.getSize();
//            String suffix = fileInfoEntity.getSuffix();
//            String time = fileInfoEntity.getTime();
//            JsonObject jsonObject = new JsonObject()
//                .put("name", name)
//                .put("size", size)
//                .put("suffix", suffix)
//                .put("uid", uid)
//                .put("time", time);
//            ctx.response()
//                .putHeader("Content-Type", "application/json")
//                .end(Json.encodePrettily(jsonObject.toString()));
//        }catch (ExecutionException | InterruptedException e){
//            e.printStackTrace();
//        }
    }

    // TODO 检查下两个文件都生成了么，内容是否完整，要求需要有匹配的单元测试
    // 保存文件
    public String save(File file) {
        String saveUid = generateBsyUid();
        String saveFileName = file.getName();
        long saveSize = file.length();
        String saveFileNameUid = suffixFileNameWithN(saveUid);
        String saveFileContextUid = suffixFileNameWithO(saveUid);
        // 写入文件名
        vertx.fileSystem().writeFile(config.uploadPath() + saveFileNameUid, Buffer.buffer(originalFileName));
        vertx.fileSystem().copy(file.getAbsolutePath(), config.uploadPath() + saveFileContextUid);
        vertx.fileSystem().delete(file.getAbsolutePath());
        return saveUid;
    }

    //TODO get\fetch\ 都要比obtain更合适，同时需要考虑文件不存在的情况，还有就是不要把原始File在java层返回，
    // 我们不希望获取到File 的java代码可以修改我们的原始文件，所以应该复制一份
    // 准备对应的单元测试
    // 获取文件
    public File obtain(String uid) {
        String contentFile = suffixFileNameWithO(uid);
        String contentFilePath = config.uploadPath() + contentFile;
        File file = new File(contentFilePath);
        return file;
    }

    //TODO delete 更简单，另外就是考虑文件不存在的情况，准备对应的单元测试
    // 丢弃服务器端中的文件
    public boolean drop(String uid) {
        String deleteNamePath = suffixFileNameWithN(config.uploadPath() + uid);
        String deleteContentPath = suffixFileNameWithO(config.uploadPath() + uid);
        File fileN = new File(deleteNamePath);
        File fileO = new File(deleteContentPath);
        boolean isDelete1 = fileN.delete();
        boolean isDelete2 = fileO.delete();
        return isDelete1 && isDelete2;
    }

//    public FileInfoEntity show(String uid) throws ExecutionException, InterruptedException {
//        String fileNamePath = suffixFileNameWithN(config.uploadPath() + uid);
//        String fileContentPath = suffixFileNameWithO(config.uploadPath() + uid);
//        File file = new File(fileNamePath);
//        File fileContent = new File(fileContentPath);
//        CompletableFuture completableFuture = new CompletableFuture();
//        vertx.fileSystem().readFile(fileNamePath, result -> {
//            if (result.succeeded()) {
//                Buffer buffer = result.result();
//                String line = buffer.toString("UTF-8");
//                String suffix = line.split("\\.")[1];
//                Path path = Paths.get(fileNamePath);
//                String createTime = "00:00";
//                try {
//                    BasicFileAttributes attrs = Files.readAttributes(path, BasicFileAttributes.class);
//                    FileTime creationTime = attrs.creationTime();
//                    createTime = creationTime.toString();
//                } catch (IOException e) {
//                    logger.error("Failed to read file attributes", e);
//                }
//                
//                FileInfoEntity entity = new FileInfoEntity(line, fileContent.length(), suffix, uid, createTime);
//                completableFuture.complete(entity);
//            } else {
//                logger.error("Failed to read file name: " + result.cause());
//            }
//        });
//
//        return (FileInfoEntity) completableFuture.get();
//    }

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
