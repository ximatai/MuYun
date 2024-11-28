package net.ximatai.muyun.http;

import io.quarkus.vertx.web.RouteFilter;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.RouterFilterPriority;
import net.ximatai.muyun.core.config.MuYunConfig;

import static net.ximatai.muyun.MuYunConst.USE_GATEWAY_CONTEXT_KEY;

@ApplicationScoped
public class MarkFilter {
    @Inject
    MuYunConfig config;

    @RouteFilter(RouterFilterPriority.MARK)
    void filter(RoutingContext context) {
        context.put(USE_GATEWAY_CONTEXT_KEY, config.gatewayMode());
        context.next();
    }

}
