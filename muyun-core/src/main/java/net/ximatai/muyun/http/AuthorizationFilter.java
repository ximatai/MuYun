package net.ximatai.muyun.http;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.MultiMap;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.RouterFilterPriority;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IAuthorizationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class AuthorizationFilter implements IRuntimeAbility {

    @ConfigProperty(name = "quarkus.rest.path")
    String restPath;

    @Inject
    RoutingContext routingContext;

    @Inject
    Instance<IAuthorizationService> authorizationService;

    @Inject
    MuYunConfig config;

    @RouteFilter(RouterFilterPriority.AUTHORIZATION)
    void filter(RoutingContext context) {
        String path = context.request().path();

        if (path.startsWith(restPath)) { //仅对 /api开头的请求做权限拦截

            if (config.debug()) {
                MultiMap headers = context.response().headers();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, PATCH, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "X-Requested-With, Content-Type, Authorization");
                headers.add("Access-Control-Expose-Headers", "Content-Disposition");

                routingContext.put("debug", true);
            }

            IRuntimeUser runtimeUser = this.getUser();
            if ("/".equals(restPath)) {
                // api 根路径不是 /api 而是 / ，会影响后续url拆分，所以要前面补充一个 /api
                // 主要是为了应对单元测试的存量代码
                path = "/api" + path;
            }
            ApiRequest apiRequest = new ApiRequest(runtimeUser, path);

            if (authorizationService.isResolvable() && !authorizationService.get().isAuthorized(apiRequest)) {
                int code = runtimeUser.getId().equals(IRuntimeUser.WHITE.getId()) ? 401 : 403; //说明是白名单用户，也就是登录过期情况
                context.response().setStatusCode(code).end(apiRequest.getError().getMessage());
//                context.fail(code,apiRequest.getError());
                return;
            }

            context.put(MuYunConst.API_REQUEST_CONTEXT_KEY, apiRequest);
        }

        context.next();
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
