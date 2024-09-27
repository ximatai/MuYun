package net.ximatai.muyun.server;

import io.quarkus.runtime.StartupEvent;
import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class VertxRouterSetup {

    public void init(@Observes StartupEvent ev, Vertx vertx, Router router) {
        router.route().handler(SessionHandler.create(LocalSessionStore.create(vertx)));
    }
}
