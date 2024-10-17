package net.ximatai.muyun.http;

import io.quarkus.runtime.StartupEvent;
import io.vertx.ext.web.Router;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.WebConfig;

public class RedirectRouter {

    @Inject
    WebConfig webConfig;

    void installRoute(@Observes StartupEvent startupEvent, Router router) {
        webConfig.redirects().forEach(r -> {
            router.route(r.from())
                .handler(rc -> {
                    rc.response().setStatusCode(301)
                        .putHeader("Location", r.to())
                        .end();
                });

        });
    }

}
