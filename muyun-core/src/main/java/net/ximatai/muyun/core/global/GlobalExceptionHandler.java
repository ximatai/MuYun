package net.ximatai.muyun.core.global;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.NotAllowedException;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.IToFrontendException;
import net.ximatai.muyun.core.exception.PermsException;
import net.ximatai.muyun.database.core.exception.MuYunDatabaseException;
import net.ximatai.muyun.model.IRuntimeUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static jakarta.ws.rs.core.Response.Status.*;

@Provider
public class GlobalExceptionHandler implements ExceptionMapper<Throwable>, IRuntimeAbility {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @Inject
    MuYunConfig config;

    @Inject
    UriInfo uriInfo;

    @Inject
    RoutingContext routingContext;

    @Override
    public Response toResponse(Throwable e) {
        // 默认的响应状态是内部服务器错误
        Response.Status responseStatus = INTERNAL_SERVER_ERROR;
        String requestPath = uriInfo.getRequestUri().getPath();

        LOGGER.error("USER:{},URI:{}", getUser().getId(), requestPath);

        if (!config.isProdMode()) {
            e.printStackTrace();
        }

        String message = "服务器错误，请检查";

        if (!isProdMode() && e.getMessage() != null) { // 非生产环境可以访问原始错误信息
            message = e.getMessage();
        }

        if (e instanceof IToFrontendException toFrontendException) {
            message = toFrontendException.getMessage();
        }

        if (e instanceof NullPointerException exception && exception.getMessage() != null) {
            message = exception.getMessage();
        } else if (e instanceof PermsException) { // 权限报错
            responseStatus = getUser().getId().equals(IRuntimeUser.WHITE.getId()) ? UNAUTHORIZED : FORBIDDEN; //说明是白名单用户，也就是登录过期情况
        } else if (e instanceof MuYunDatabaseException exception) {
            switch (exception.getType()) {
                case DATA_NOT_FOUND -> responseStatus = Response.Status.NOT_FOUND;
                case DEFAULT -> responseStatus = Response.Status.BAD_REQUEST;
            }
            message = exception.getMessage();
        } else if (e instanceof NotFoundException) {
            responseStatus = Response.Status.NOT_FOUND;
        } else if (e instanceof NotAllowedException) {
            responseStatus = Response.Status.METHOD_NOT_ALLOWED;
        } else {
            LOGGER.error("ERROR DETAIL:", e);
        }

        return Response
            .status(responseStatus)
            .entity(message)
            .build();
    }

}
