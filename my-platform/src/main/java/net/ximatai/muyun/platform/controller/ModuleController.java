package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.core.ability.curd.ICURDAbility;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.standard.DataAccess;

@Path("/module")
public class ModuleController implements ICURDAbility {
    
    @Inject
    DataAccess dataAccess;

    @Override
    public IDatabaseAccess getDatabase() {
        return dataAccess;
    }

    @Override
    public String getMainTable() {
        return "app_module";
    }
}
