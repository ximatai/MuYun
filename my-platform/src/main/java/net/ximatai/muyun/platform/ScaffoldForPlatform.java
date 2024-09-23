package net.ximatai.muyun.platform;

import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;

public abstract class ScaffoldForPlatform extends Scaffold implements IMetadataAbility, ICURDAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "platform";
    }
}
