package net.ximatai.muyun.ability;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import net.ximatai.muyun.MuYunConst;
import net.ximatai.muyun.core.config.IProfile;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.IRuntimeUser;
import org.slf4j.LoggerFactory;

import static net.ximatai.muyun.MuYunConst.USE_GATEWAY_CONTEXT_KEY;

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
            boolean gatewayMode = this.getRoutingContext().get(USE_GATEWAY_CONTEXT_KEY);
            if (gatewayMode) {
                String gwUser = this.getRoutingContext().request().getHeader("gw-user");

                if (gwUser != null) {
                    return IRuntimeUser.build(new JsonObject(gwUser));
                }
            }

            Session session = getRoutingContext().session();
            if (session != null && session.get(MuYunConst.SESSION_USER_KEY) != null) {
                if (gatewayMode) {
                    LoggerFactory.getLogger(IRuntimeAbility.class).warn("检测到您在使用 session 控制登录信息，建议关闭 gateway 模式，参考配置 muyun:gateway:false");
                }
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
