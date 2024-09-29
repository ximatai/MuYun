package net.ximatai.muyun.platform;

import net.ximatai.muyun.base.BaseScaffold;

import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

public abstract class ScaffoldForPlatform extends BaseScaffold {

    @Override
    public String getSchemaName() {
        return DB_SCHEMA;
    }
}
