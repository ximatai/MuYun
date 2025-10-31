package net.ximatai.muyun.platform.checker;

import net.ximatai.muyun.core.exception.LoginException;
import net.ximatai.muyun.model.IRuntimeUser;

/**
 * 额外登录检查器接口，可以实现此接口来添加自定义身份验证成功后的登录检查逻辑
 */
public interface IExtraLoginChecker {
    /**
     * @param runtimeUser 当前登录用户
     * @throws LoginException 如果检查未通过则抛出登录异常
     */
    void check(IRuntimeUser runtimeUser) throws LoginException;
}
