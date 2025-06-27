package net.ximatai.muyun.http;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.vertx.web.RouteFilter;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IRuntimeProvider;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.util.Optional;

import static net.ximatai.muyun.MuYunConst.*;

@ApplicationScoped
public class RootFilter {

    @ConfigProperty(name = "quarkus.rest.path")
    String restPath;

    @Inject
    MuYunConfig config;

    @Inject
    Instance<IRuntimeProvider> runtimeProvider;

    private SessionHandler sessionHandler;

    void init(@Observes StartupEvent ev, Vertx vertx) {
        if (config.useSession()) {
            int hour = config.sessionTimeoutHour();
            long timeOut = (long) hour * 60 * 60 * 1000;
            sessionHandler = SessionHandler.create(LocalSessionStore.create(vertx)).setSessionTimeout(timeOut).setCookieHttpOnlyFlag(true);
        }
    }

    @RouteFilter(Priorities.USER + 100)
    void sessionFilter(RoutingContext context) {
        String path = context.request().path();
        if (config.useSession() && path.startsWith(restPath)) { // 只有 /api的请求需要考虑 session
            sessionHandler.handle(context);
        } else {
            context.next();
        }
    }

    @RouteFilter(Priorities.USER)
    void userFilter(RoutingContext context) {
        IRuntimeUser runtimeUser = resolveRuntimeUser(context);

        context.put(CONTEXT_KEY_RUNTIME_USER, runtimeUser);

        context.next();
    }

    @RouteFilter(Priorities.HEADER_DECORATOR)
    void apiRequestFilter(RoutingContext context) {
        IRuntimeUser runtimeUser = context.get(CONTEXT_KEY_RUNTIME_USER);

        if (runtimeProvider.isResolvable()) {
            ApiRequest apiRequest = runtimeProvider.get().apiRequest(context);
            apiRequest.setUser(runtimeUser);
            context.put(CONTEXT_KEY_API_REQUEST, apiRequest);
        } else {
            context.put(CONTEXT_KEY_API_REQUEST, ApiRequest.BLANK);
        }

        context.put(CONTEXT_KEY_REQUEST_HOST, getHost(context.request().headers(), context.request().authority().host()));

        context.next();
    }

    private IRuntimeUser resolveRuntimeUser(RoutingContext context) {
        if (runtimeProvider.isResolvable()) {
            Optional<IRuntimeUser> userOptional = runtimeProvider.get().getUser(context);
            if (userOptional.isPresent()) {
                return userOptional.get();
            } else if (config.isTestMode() // 只有测试模式需要手动提供 userID 放在 Header里
                && context.request().getHeader("userID") != null) {
                String userID = context.request().getHeader("userID");
                return IRuntimeUser.build(userID);
            }
        }

        return IRuntimeUser.WHITE;
    }

    private String getHost(MultiMap headers, String baseHost) {
        String host = headers.get("X-Forwarded-For");

        // 回退到标准的 `Forwarded` 头
        if (host == null || host.isEmpty()) {
            String forwardedHeader = headers.get("Forwarded");
            if (forwardedHeader != null) {
                // 从 `Forwarded` 头提取 `host`
                String[] forwardedParts = forwardedHeader.split(";");
                for (String part : forwardedParts) {
                    if (part.trim().startsWith("for=")) {
                        host = part.split("=")[1].trim();
                        break;
                    }
                }
            }
        }

        // 最后回退到标准的 `Host` 头
        if (host == null || host.isEmpty()) {
            host = baseHost;
        }

        return host;
    }

}
