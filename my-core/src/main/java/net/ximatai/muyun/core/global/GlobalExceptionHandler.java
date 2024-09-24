package net.ximatai.muyun.core.global;

import jakarta.inject.Inject;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.core.ServerConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Exception> {

    private final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Inject
    ServerConfig config;

    @Inject
    UriInfo uriInfo;

    @Override
    public Response toResponse(Exception e) {
        // 默认的响应状态是内部服务器错误
        Response.Status responseStatus = Response.Status.INTERNAL_SERVER_ERROR;
        String requestPath = uriInfo.getRequestUri().toString();
        logger.error("{}@{}", e.getMessage(), requestPath, e);

        String message = config.debug() ? e.getMessage() : "服务器错误，请检查。";

        if (e instanceof MyException exception) {
            message = exception.getMessage();
        } else if (e instanceof MyDatabaseException myDatabaseException) {
            switch (myDatabaseException.getType()) {
                case DATA_NOT_FOUND -> responseStatus = Response.Status.NOT_FOUND;
                case DEFAULT -> responseStatus = Response.Status.BAD_REQUEST;
            }
        } else if (e instanceof NotFoundException) {
            responseStatus = Response.Status.NOT_FOUND;
        }

        return Response
            .status(responseStatus)
            .entity(message)
            .build();
    }
}
