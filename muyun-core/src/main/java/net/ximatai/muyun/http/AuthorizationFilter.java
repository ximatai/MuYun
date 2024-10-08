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
import net.ximatai.muyun.core.MuYunConfig;
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
            }

            IRuntimeUser runtimeUser = this.getUser();
            ApiRequest apiRequest = new ApiRequest(runtimeUser, path);

            if (authorizationService.isResolvable() && !authorizationService.get().isAuthorized(apiRequest)) {
                throw apiRequest.getError();
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
