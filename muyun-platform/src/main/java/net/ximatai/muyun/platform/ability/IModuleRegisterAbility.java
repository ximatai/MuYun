package net.ximatai.muyun.platform.ability;

import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.IDeleteAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.platform.model.ModuleAction;

import java.util.ArrayList;
import java.util.List;

public interface IModuleRegisterAbility {

    ModuleController getModuleController();

    ModuleConfig getModuleConfig();

    default List<ModuleAction> defaultActions() {
        List<ModuleAction> actions = new ArrayList<>();
        actions.add(ModuleAction.MENU);
        if (this instanceof ICreateAbility) {
            actions.add(ModuleAction.CREATE);
        }
        if (this instanceof ISelectAbility) {
            actions.add(ModuleAction.VIEW);
        }
        if (this instanceof IUpdateAbility) {
            actions.add(ModuleAction.UPDATE);
        }
        if (this instanceof IDeleteAbility) {
            actions.add(ModuleAction.DELETE);
        }
        if (this instanceof ISortAbility) {
            actions.add(ModuleAction.SORT);
        }
        return actions;
    }

    default void registerModule() {
        ModuleConfig config = getModuleConfig();
        ModuleController moduleController = getModuleController();
        if (config != null && moduleController != null) {
            config.addActions(defaultActions());
            moduleController.register(config);
        }
    }
}
