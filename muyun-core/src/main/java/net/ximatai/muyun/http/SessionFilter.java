package net.ximatai.muyun.http;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.RouterFilterPriority;
import net.ximatai.muyun.core.config.MuYunConfig;
import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class SessionFilter {

    @ConfigProperty(name = "quarkus.rest.path")
    String restPath;

    @Inject
    MuYunConfig config;

    private SessionHandler sessionHandler;

    void init(@Observes StartupEvent ev, Vertx vertx) {
        int hour = config.sessionTimeoutHour();
        long timeOut = (long) hour * 60 * 60 * 1000;
        sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx)).setSessionTimeout(timeOut);
    }

    @RouteFilter(RouterFilterPriority.SESSION_BUILDER)
    void filter(RoutingContext context) {
        String path = context.request().path();
        if (path.startsWith(restPath)) { // 只有 /api的请求需要考虑 session
            sessionHandler.handle(context);
        } else {
            context.next();
        }
    }

}
