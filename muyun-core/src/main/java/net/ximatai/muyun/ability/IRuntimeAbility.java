package net.ximatai.muyun.ability;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.model.IRuntimeUser;

public interface IRuntimeAbility {

    String SESSION_USER_KEY = "user";

    RoutingContext getRoutingContext();

    default IRuntimeUser getUser() {
        Session session = getRoutingContext().session();
        if (session != null && session.get(SESSION_USER_KEY) != null) {
            return session.get(SESSION_USER_KEY);
        } else {
            return IRuntimeUser.WHITE;
        }
    }

    default void setUser(IRuntimeUser user) {
        getRoutingContext().session().put(SESSION_USER_KEY, user);
    }

    default void destroy() {
        getRoutingContext().session().destroy();
    }

}
