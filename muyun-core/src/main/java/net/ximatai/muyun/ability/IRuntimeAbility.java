package net.ximatai.muyun.ability;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.model.IRuntimeUser;

public interface IRuntimeAbility {

    RoutingContext getRoutingContext();

    default IRuntimeUser getUser() {
        Session session = getRoutingContext().session();
        if (session != null && session.get(MuYunConst.SESSION_USER_KEY) != null) {
            return session.get(MuYunConst.SESSION_USER_KEY);
        } else {
            return IRuntimeUser.WHITE;
        }
    }

    default void setUser(IRuntimeUser user) {
        getRoutingContext().session().put(MuYunConst.SESSION_USER_KEY, user);
    }

    default void destroy() {
        getRoutingContext().session().destroy();
    }

}
