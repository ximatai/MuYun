package net.ximatai.muyun.fileserver;

import io.quarkus.runtime.Startup;
import io.quarkus.runtime.configuration.MemorySizeConverter;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@Startup
@ApplicationScoped
public class FileServerRegister {

    @Inject
    FileServerConfig config;
    
    @ConfigProperty(name = "quarkus.http.limits.max-body-size")
    String maxBodySize;
    
    MemorySizeConverter converter = new MemorySizeConverter();

    private String getUploadPath() {
        String uploadPath = config.uploadPath();
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath = uploadPath + "/";
        }
        return uploadPath;
    }

    @RouteFilter(10)
        // 该注解中有route
    void filter(RoutingContext context) {
        BodyHandler.create()
            .setUploadsDirectory(getUploadPath())
            .setBodyLimit(converter.convert(maxBodySize).asLongValue())
            .handle(context);
        // BodyHandler是一个类，对象handler可以作为route的参数
        // create()函数返回一个BodyHandlerImpl
        // BodyHandlerImpl中有handle方法
        // handle方法接收context来处理
    }

}
