package net.ximatai.muyun.ability;

import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.core.config.IProfile;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;

/**
 * 获取运行时上下文的能力
 */
public interface IRuntimeAbility extends IProfile {

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
            } else if (isTestMode() // 只有测试模式需要手动提供 userID 放在 Header里
                && getRoutingContext().request().getHeader("userID") != null) {
                String userID = getRoutingContext().request().getHeader("userID");
                return IRuntimeUser.build(userID);
            } else {
                return IRuntimeUser.WHITE;
            }
        } catch (Exception e) {
            return IRuntimeUser.WHITE;
        }
    }

    default void setUserInContext(IRuntimeUser user) {
        getRoutingContext().session().put(MuYunConst.SESSION_USER_KEY, user);
    }

    default void destroy() {
        getRoutingContext().session().destroy();
    }

}
