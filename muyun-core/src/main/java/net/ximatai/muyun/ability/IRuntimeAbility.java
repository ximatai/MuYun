package net.ximatai.muyun.ability;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;

/**
 * 获取运行时上下文的能力
 */
public interface IRuntimeAbility {

    RoutingContext getRoutingContext();

    default ApiRequest getApiRequest() {
        try {
            return getRoutingContext().get(MuYunConst.API_REQUEST_CONTEXT_KEY);
        } catch (Exception e) {
            return ApiRequest.BLANK;
        }
    }

    default IRuntimeUser getUser() {
        try {
            Session session = getRoutingContext().session();
            if (session != null && session.get(MuYunConst.SESSION_USER_KEY) != null) {
                return session.get(MuYunConst.SESSION_USER_KEY);
            } else {
                return IRuntimeUser.WHITE;
            }
        } catch (Exception e) {
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
