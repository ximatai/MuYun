package net.ximatai.muyun.ability;

import io.quarkus.arc.Arc;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.fileserver.exception.FileException;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public interface IFileAbility {

    default IFileService getFileService() {
        return Arc.container().instance(IFileService.class).get();
    }

    List<String> fileColumns();

    default void checkFileBelongsToData(String fileID, String dataID) {
        boolean exists = false;
        if (this instanceof ISelectAbility ability) {
            Map<String, Object> map = ability.view(dataID);
            if (map == null) {
                throw new MuYunException("该文件不存在");
            }
            for (String col : fileColumns()) {
                Object o = map.get(col);
                if (o instanceof String str && str.contains(fileID)) {
                    exists = true;
                }
                if (o instanceof String[] arr && List.of(arr).contains(fileID)) {
                    exists = true;
                }
                if (o instanceof List<?> list && list.contains(dataID)) {
                    exists = true;
                }
            }

        }

        if (!exists) {
            throw new MuYunException("该文件不存在");
        }
    }

    @GET
    @Path("/download/{id}")
    @Operation(summary = "返回需要下载文件对应的临时下载路径")
    default Response download(@PathParam("id") String id, @QueryParam("fileID") String fileID) {

        checkFileBelongsToData(fileID, id);
        try {
            File file = getFileService().get(fileID);
            String name = getFileService().info(fileID).getName();
            return Response.ok(file)
                .header("Content-Disposition", "attachment; filename*=UTF-8''" + URLEncoder.encode(name, StandardCharsets.UTF_8))
                .header("Content-type", "application/octet-stream")
                .build();

        } catch (FileException e) {
            return Response.status(Response.Status.NOT_FOUND).entity("File not found").build();
        }

    }

    @POST
    @Path("/upload")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Operation(summary = "上传文件并返回存储路径")
    default String upload(@RestForm("file") FileUpload fileUpload) {
        try {
            return getFileService().save(fileUpload.uploadedFile().toFile(), fileUpload.fileName());
        } catch (Exception e) {
            throw new WebApplicationException("文件上传失败", Response.Status.INTERNAL_SERVER_ERROR);
        }
    }

}
