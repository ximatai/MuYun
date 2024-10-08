package net.ximatai.muyun.platform;

import net.ximatai.muyun.ability.IAuthAbility;
import net.ximatai.muyun.base.BaseScaffold;

import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

public abstract class ScaffoldForPlatform extends BaseScaffold implements IAuthAbility {

    @Override
    public String getSchemaName() {
        return DB_SCHEMA;
    }

}
