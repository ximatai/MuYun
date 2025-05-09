package net.ximatai.muyun.base;

import net.ximatai.muyun.ability.IAuthAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;

public abstract class BaseScaffold extends Scaffold implements IMetadataAbility, ICURDAbility, ITableCreateAbility, IAuthAbility {

}
