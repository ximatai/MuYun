package net.ximatai.muyun.http;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.log.LogItem;
import net.ximatai.muyun.service.ILogAccess;
import net.ximatai.muyun.service.ILogError;
import net.ximatai.muyun.service.ILogLogin;
import net.ximatai.muyun.util.UserAgentParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

@Provider
@ApplicationScoped
public class LogFilter implements ContainerRequestFilter, ContainerResponseFilter, IRuntimeAbility {

    @Inject
    RoutingContext routingContext;

    @Inject
    Instance<ILogAccess> iLogAccess;

    @Inject
    Instance<ILogError> iLogError;

    @Inject
    Instance<ILogLogin> iLogLogin;

    private static final Logger LOG = LoggerFactory.getLogger(LogFilter.class);

    // 定义一个常量用于存储开始时间的键
    private static final String START_TIME_KEY = "startTime";

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 记录请求开始时间，并存储在 RoutingContext 中
        routingContext.put(START_TIME_KEY, Instant.now().toEpochMilli());
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        ApiRequest apiRequest = getApiRequest();
        if (apiRequest == null) {
            return;
        }

        String moduleName = apiRequest.getModuleName();
        String actionName = apiRequest.getActionName();
        if (moduleName == null || actionName == null) {
            return;
        }

        Long startTime = routingContext.get(START_TIME_KEY);
        Long costTime = startTime != null ? Instant.now().toEpochMilli() - startTime : null;

        IRuntimeUser user = getUser();
        String userID = user.getId();
        String userAgent = requestContext.getHeaderString("User-Agent");
        String os = UserAgentParser.getOS(userAgent);
        String browser = UserAgentParser.getBrowser(userAgent);
        String uri = requestContext.getUriInfo().getRequestUri().getPath();
        String method = requestContext.getMethod();
        Map params = requestContext.getUriInfo().getQueryParameters();
        int status = responseContext.getStatus();

        // 创建 LogBaseItem 并填充响应信息
        LogItem logItem = new LogItem()
            .setUserID(userID)
            .setUsername(apiRequest.getUsername())
            .setModuleName(moduleName)
            .setActionName(actionName)
            .setDataID(apiRequest.getDataID())
            .setUri(uri)
            .setMethod(method)
            .setHost(getRequestHost())
            .setUserAgent(userAgent)
            .setCostTime(costTime)
            .setOs(os)
            .setBrowser(browser)
            .setStatusCode(status)
            .setSuccess(status < 400);

        if (!params.isEmpty()) {
            logItem.setParams(params);
        }

        if (MuYunConst.SSO_MODULE_NAME.equals(moduleName) && iLogLogin.isResolvable()) {
            if (apiRequest.getError() != null) {
                logItem.setError(apiRequest.getError().getMessage()); // 对应登录失败的日志
            }
            iLogLogin.get().log(logItem);
        } else if (status > 400 && iLogError.isResolvable()) {
            logItem.setError(responseContext.getEntity() != null ? responseContext.getEntity().toString() : "Unknown error");
            iLogError.get().log(logItem);
        } else if (iLogAccess.isResolvable()) {
            iLogAccess.get().log(logItem);
        }

    }

}
