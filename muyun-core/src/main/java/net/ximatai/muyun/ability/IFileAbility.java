package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;
import org.apache.commons.codec.digest.DigestUtils;
import org.eclipse.microprofile.openapi.annotations.Operation;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public interface IFileAbility {

    @GET
    @Path("/download/{id}")
    @Operation(summary = "返回需要下载文件对应的临时下载路径")
    default Response delete(@PathParam("id") String id, @QueryParam("fileID") String fileID) {

        String sign = sign(id, LocalDateTime.now());

        return Response.ok()
            .status(Response.Status.FOUND)
            .header("Location", String.format("/fileServer/download/%s?sign=%s", fileID, sign))
            .build();
    }

    /**
     * 签名算法
     *
     * @param id 文件ID
     * @return DigestUtils.md5Hex(id + 距离此刻最近的一个整00分或30分的时间戳)
     */
    default String sign(String id, LocalDateTime time) {
//        LocalDateTime time = LocalDateTime.time();
        int minutes = time.getMinute();

        // 判断分钟数靠近 00 分还是 30 分
        int roundedMinutes;
        if (minutes <= 15) {
            // 如果当前时间在 0-15 分之间，靠近 00 分
            roundedMinutes = 0;
        } else if (minutes <= 45) {
            // 如果当前时间在 16-45 分之间，靠近 30 分
            roundedMinutes = 30;
        } else {
            // 如果当前时间在 46-59 分之间，靠近下一个小时的 00 分
            roundedMinutes = 0;
            time = time.plusHours(1); // 加1小时
        }

        // 设置靠近的分钟数，并将秒数和纳秒数置为 0
        LocalDateTime nearestTime = time.withMinute(roundedMinutes).withSecond(0).withNano(0);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        String timeStamp = nearestTime.format(formatter);
        return DigestUtils.md5Hex(id + timeStamp);
    }
}
