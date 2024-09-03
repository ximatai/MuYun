package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.database.IDatabaseAccess;

@Path("/module")
public class ModuleController implements ICURDAbility {

    @Inject
    IDatabaseAccess databaseAccess;

    @Override
    public String getSchemaName() {
        return "platform";
    }

    @Override
    public String getMainTable() {
        return "app_module";
    }

    @Override
    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }
}
