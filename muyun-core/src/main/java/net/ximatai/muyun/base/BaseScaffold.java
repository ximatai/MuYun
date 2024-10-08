package net.ximatai.muyun.base;

import jakarta.enterprise.inject.Instance;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IAuthAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.service.IAuthorizationService;

public abstract class BaseScaffold extends Scaffold implements IMetadataAbility, ICURDAbility, ITableCreateAbility, IAuthAbility {

    @Inject
    Instance<IAuthorizationService> authorizationService;

    @Override
    public IAuthorizationService getAuthorizationService() {
        if (authorizationService.isResolvable()) {
            return authorizationService.get();
        } else {
            return null;
        }
    }

}
