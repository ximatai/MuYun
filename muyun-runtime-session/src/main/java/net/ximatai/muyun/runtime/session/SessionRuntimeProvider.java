package net.ximatai.muyun.runtime.session;

import io.vertx.ext.web.RoutingContext;
import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.service.IRuntimeProvider;

import java.util.Optional;

@ApplicationScoped
public class SessionRuntimeProvider implements IRuntimeProvider {
    @Override
    public Optional<IRuntimeUser> getUser(RoutingContext context) {
        return Optional.ofNullable(context.session())
            .map(s -> s.get(MuYunConst.SESSION_USER_KEY));
    }
}
