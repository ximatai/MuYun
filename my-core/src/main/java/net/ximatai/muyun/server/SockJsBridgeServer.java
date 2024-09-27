package net.ximatai.muyun.server;

import io.quarkus.vertx.web.Route;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.bridge.PermittedOptions;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.sockjs.SockJSBridgeOptions;
import io.vertx.ext.web.handler.sockjs.SockJSHandler;
import io.vertx.ext.web.handler.sockjs.SockJSHandlerOptions;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;

@ApplicationScoped
public class SockJsBridgeServer {

    @Inject
    Vertx vertx;

    @Inject
    EventBus eventBus;

    void init(@Observes Router router) {
        SockJSHandlerOptions options = new SockJSHandlerOptions();
        SockJSHandler sockJSHandler = SockJSHandler.create(vertx, options);

        router.route("/eventbus/*")
            .subRouter(sockJSHandler.bridge(createBridgeOptions()));
    }

    private SockJSBridgeOptions createBridgeOptions() {
        SockJSBridgeOptions options = new SockJSBridgeOptions();
        options.addInboundPermitted(new PermittedOptions().setAddress("web\\..+"));
        options.addOutboundPermitted(new PermittedOptions().setAddress("web\\..+"));
        options.addOutboundPermitted(new PermittedOptions().setAddressRegex("data.change\\..+").setMatch(new JsonObject().put("toFrontEnd", true)));
        return options;
    }

    // 处理 POST 请求，向 EventBus 发布消息
    @Route(path = "/test", methods = Route.HttpMethod.GET)
    public void sendMessage(RoutingContext context) {
        // 异步发送消息到 "news-feed" 地址
        eventBus.publish("data.change.test", "hello world");

        context.response().end("hello world");
    }
}
