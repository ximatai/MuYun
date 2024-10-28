package net.ximatai.muyun.platform;

import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;

import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

public abstract class ScaffoldForPlatform extends BaseScaffold {

    @Override
    public String getSchemaName() {
        return DB_SCHEMA;
    }

    @Override
    protected void afterInit() {
        super.afterInit();
        if (this instanceof IModuleRegisterAbility ability) {
            ability.registerModule();
        }
    }
}
