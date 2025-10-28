package net.ximatai.muyun.platform.checker;

import net.ximatai.muyun.model.IRuntimeUser;

public interface IExtraAuthChecker {
    void check(IRuntimeUser runtimeUser);
}
