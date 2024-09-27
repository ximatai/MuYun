package net.ximatai.muyun.ability;

import io.vertx.ext.web.RoutingContext;
import net.ximatai.muyun.model.IRuntimeUser;

public interface IRuntimeAbility {

    String SESSION_USER_KEY = "user";

    RoutingContext getRoutingContext();

    default IRuntimeUser getUser() {
        return getRoutingContext().session().get(SESSION_USER_KEY);
    }

    default void setUser(IRuntimeUser user) {
        getRoutingContext().session().put(SESSION_USER_KEY, user);
    }

    default void destroy() {
        getRoutingContext().session().destroy();
    }

}
