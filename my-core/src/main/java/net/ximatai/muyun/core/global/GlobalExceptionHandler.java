package net.ximatai.muyun.core.global;

import jakarta.inject.Inject;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.core.ServerConfig;
import net.ximatai.muyun.database.exception.MyDatabaseException;
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

        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;

        if (e instanceof MyDatabaseException) {
            switch (((MyDatabaseException) e).getType()) {
                case DATA_NOT_FOUND -> responseStatus = Response.Status.NOT_FOUND;
                case DEFAULT -> responseStatus = Response.Status.BAD_REQUEST;
            }
        }

        return Response
            .status(responseStatus)
            .entity(config.debug() ? e.getMessage() : "服务器错误，请检查。")
            .build();
    }
}
