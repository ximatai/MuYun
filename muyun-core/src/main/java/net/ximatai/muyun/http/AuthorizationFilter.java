package net.ximatai.muyun.http;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IRuntimeAbility;
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

    @RouteFilter(99)
    void filter(RoutingContext context) {
        String path = context.request().path();
        IRuntimeUser runtimeUser = this.getUser();
        if (authorizationService.isResolvable()) {
            authorizationService.get().isAuthorized(runtimeUser, null, null);
        }
        context.next();
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
