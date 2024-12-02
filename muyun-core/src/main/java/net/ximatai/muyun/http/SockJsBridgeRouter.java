package net.ximatai.muyun.http;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

@ApplicationScoped
public class SockJsBridgeRouter {

    void init(@Observes Router router, Vertx vertx) {
        SockJSHandlerOptions options = new SockJSHandlerOptions();
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        router.route("/api/eventbus/*")
            .subRouter(sockJSHandler.bridge(createBridgeOptions()));
    }

    private SockJSBridgeOptions createBridgeOptions() {
        SockJSBridgeOptions options = new SockJSBridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("web\\..+"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("web\\..+"));
        options.addOutboundPermitted(new PermittedOptions().setAddressRegex("data.change\\..+").setMatch(new JsonObject().put("toFrontEnd", true)));
        return options;
    }

}
