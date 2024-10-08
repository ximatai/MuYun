package net.ximatai.muyun.http;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.ximatai.muyun.RouterFilterPriority;

@ApplicationScoped
public class SessionFilter {

    private SessionHandler sessionHandler;

    void init(@Observes StartupEvent ev, Vertx vertx) {
        sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx));
    }

    @RouteFilter(RouterFilterPriority.SESSION_BUILDER)
    void filter(RoutingContext context) {
        sessionHandler.handle(context);
    }

}
