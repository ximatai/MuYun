package net.ximatai.muyun.proxy;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.WebSocketConnectOptions;
import io.vertx.ext.web.Router;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.inject.Inject;
import net.ximatai.muyun.proxy.model.ProxyConfig;
import net.ximatai.muyun.proxy.model.UpstreamItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationScoped
public class MuYunProxy {

    private final Logger logger = LoggerFactory.getLogger(MuYunProxy.class);

    @Inject
    ProxyConfig config;

    @Inject
    Router router;

    @Inject
    Vertx vertx;

    void init(@Observes Router router) {
        for (UpstreamItem item : config.upstreams()) {
            mount(item);
        }
    }

    private void mount(UpstreamItem item) {
        Upstream upstream = new Upstream(item.url(), 1, vertx);

        String prefix = item.prefix();

        if (item.prefix().endsWith("/")) {
            prefix = item.prefix() + "*";
        }

        router.route(prefix)
            .handler(routingContext -> {

                HttpServerRequest req = routingContext.request();
                HttpServerResponse resp = routingContext.response();

                req.pause();

                String uri = req.uri().replaceFirst(item.prefix(), upstream.getPath());
                HttpClient upstreamClient = upstream.getClient();

                String upgrade = req.getHeader("Upgrade");
                if ("websocket".equalsIgnoreCase(upgrade)) {
                    Future<ServerWebSocket> fut = req.toWebSocket();
                    fut.onSuccess(ws -> {
                        WebSocketConnectOptions webSocketConnectOptions = new WebSocketConnectOptions();
                        webSocketConnectOptions.setHost(upstream.getHost());
                        webSocketConnectOptions.setPort(upstream.getPort());
                        webSocketConnectOptions.setURI(uri);
                        webSocketConnectOptions.setHeaders(
                            req.headers()
                                .remove("host")
//                                            .remove("sec-websocket-extensions")
                        );

                        vertx.createWebSocketClient()
                            .connect(webSocketConnectOptions)
                            .onSuccess(clientWS -> {
                                ws.frameHandler(clientWS::writeFrame);
                                ws.closeHandler(x -> {
                                    clientWS.close();
                                });
                                clientWS.frameHandler(ws::writeFrame);
                                clientWS.closeHandler(x -> {
                                    ws.close();
                                });
                            }).onFailure(err -> {
                                error(resp, err);
                            });

                    }).onFailure(err -> {
                        error(resp, err);
                    });
                } else {
                    upstreamClient.request(req.method(), uri, ar -> {
                        if (ar.succeeded()) {
                            HttpClientRequest reqUpstream = ar.result();
                            reqUpstream.headers().setAll(req.headers().remove("host"));

                            reqUpstream.send(req)
                                .onSuccess(respUpstream -> {
                                    resp.setStatusCode(respUpstream.statusCode());
                                    resp.headers().setAll(respUpstream.headers());
                                    resp.send(respUpstream);
                                }).onFailure(err -> {
                                    error(resp, err);
                                });
                        } else {
                            error(resp, ar.cause());
                        }
                    });
                }
            });
    }

    void error(HttpServerResponse resp, Throwable err) {
        logger.error(err.getMessage(), err);
        resp.setStatusCode(500).end(err.getMessage());
    }
}
