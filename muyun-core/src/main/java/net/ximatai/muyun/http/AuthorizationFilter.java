package net.ximatai.muyun.http;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.RouterFilterPriority;
import net.ximatai.muyun.ability.IRuntimeAbility;
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

    @RouteFilter(RouterFilterPriority.AUTHORIZATION)
    void filter(RoutingContext context) {
        String path = context.request().path();

        if (path.startsWith(restPath)) { //仅对 /api开头的请求做权限拦截
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
