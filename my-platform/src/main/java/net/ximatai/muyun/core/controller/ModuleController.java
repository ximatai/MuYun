package net.ximatai.muyun.core.controller;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;

@Path("/module")
public class ModuleController extends Scaffold implements ICURDAbility {

    @Override
    public String getSchemaName() {
        return "platform";
    }

    @Override
    public String getMainTable() {
        return "app_module";
    }

}
