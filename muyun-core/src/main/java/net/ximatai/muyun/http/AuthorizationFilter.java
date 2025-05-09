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
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.service.IAuthorizationService;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.IOException;

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

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String path = requestContext.getUriInfo().getRequestUri().getPath();

        if (path.startsWith(restPath)) { //仅对 /api开头的请求做权限拦截
            ApiRequest apiRequest = getApiRequest();

            if (authorizationService.isResolvable() && !authorizationService.get().isAuthorized(apiRequest)) {
                throw apiRequest.getError();
            }
        }

    }

}
