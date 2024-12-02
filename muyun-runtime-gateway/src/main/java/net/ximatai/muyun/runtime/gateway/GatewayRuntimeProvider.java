package net.ximatai.muyun.runtime.gateway;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IRuntimeProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@ApplicationScoped
public class GatewayRuntimeProvider implements IRuntimeProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(GatewayRuntimeProvider.class);

    @Override
    public Optional<IRuntimeUser> getUser(RoutingContext context) {
        return Optional.ofNullable(context.request().getHeader("gw-user"))
            .flatMap(gwUser -> {
                try {
                    // 尝试构建用户对象
                    return Optional.of(IRuntimeUser.build(new JsonObject(gwUser)));
                } catch (Exception e) {
                    // 记录解析失败的日志
                    LOGGER.warn("Failed to parse gw-user header: {}", gwUser, e);
                    return Optional.empty();
                }
            });
    }
}
