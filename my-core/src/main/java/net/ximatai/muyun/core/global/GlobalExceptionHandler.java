package net.ximatai.muyun.core.global;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.core.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Inject
    ServerConfig config;

    @Override
    public Response toResponse(Exception e) {
        logger.error(e.getMessage(), e);

        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(config.debug() ? e.getMessage() : "服务器错误，请检查。")
            .build();
    }
}
