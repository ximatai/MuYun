package net.ximatai.muyun.core.global;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.core.ServerConfig;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    @Inject
    ServerConfig config;

    @Override
    public Response toResponse(Exception e) {
        return Response
            .status(Response.Status.INTERNAL_SERVER_ERROR)
            .entity(config.debug() ? e.getMessage() : "服务器错误，请检查。")
            .build();
    }
}
