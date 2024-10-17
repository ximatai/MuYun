package net.ximatai.muyun.http;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.FileSystemAccess;
import io.vertx.ext.web.handler.StaticHandler;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.FrontendConfig;

public class StaticResources {

    @Inject
    FrontendConfig frontendConfig;

    void installRoute(@Observes StartupEvent startupEvent, Router router) {

        frontendConfig.resources()
            .forEach(item -> {
                String prefix = item.prefix();

                if (item.prefix().endsWith("/")) {
                    prefix = item.prefix() + "*";
                }

                router.route(prefix)
                    .handler(StaticHandler.create(FileSystemAccess.ROOT, item.path()));
            });

    }
}
