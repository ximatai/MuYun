package net.ximatai.muyun.http;

import io.vertx.ext.web.RoutingContext;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.ext.Provider;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IAuthorizationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

import static net.ximatai.muyun.MuYunConst.DEBUG_MODE_CONTEXT_KEY;

@Provider
@Priority(Priorities.AUTHENTICATION)
@ApplicationScoped
public class AuthorizationFilter implements ContainerRequestFilter, IRuntimeAbility {

    @ConfigProperty(name = "quarkus.rest.path")
    String restPath;

    @Inject
    RoutingContext routingContext;

    @Inject
    Instance<IAuthorizationService> authorizationService;

    @Inject
    MuYunConfig config;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        // 获取请求的路径
        String path = requestContext.getUriInfo().getRequestUri().getPath();

        if (config.debug()) {
            routingContext.put(DEBUG_MODE_CONTEXT_KEY, true);
        }

        if (path.startsWith(restPath)) { //仅对 /api开头的请求做权限拦截
            IRuntimeUser runtimeUser = this.getUser();
            if ("/".equals(restPath)) {
                // api 根路径不是 /api 而是 / ，会影响后续url拆分，所以要前面补充一个 /api
                // 主要是为了应对单元测试的存量代码
                path = "/api" + path;
            }

            ApiRequest apiRequest = new ApiRequest(runtimeUser, path);
            if (authorizationService.isResolvable() && !authorizationService.get().isAuthorized(apiRequest)) {
                throw apiRequest.getError();
            }

            routingContext.put(MuYunConst.API_REQUEST_CONTEXT_KEY, apiRequest);
        }

    }


    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
