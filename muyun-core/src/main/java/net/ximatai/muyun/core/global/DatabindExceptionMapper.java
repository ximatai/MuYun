package net.ximatai.muyun.core.global;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Provider
public class DatabindExceptionMapper implements ExceptionMapper<UnrecognizedPropertyException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DatabindExceptionMapper.class);

    @Override
    public Response toResponse(UnrecognizedPropertyException exception) {

        LOGGER.error(exception.getMessage(), exception);

        return Response.status(Response.Status.BAD_REQUEST)
            .entity("请求错误")
            .build();
    }
}
