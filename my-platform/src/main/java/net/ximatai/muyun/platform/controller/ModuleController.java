package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.uni.ICURDUniAbility;
import net.ximatai.muyun.database.IDatabaseAccessUni;

@Path("/module")
public class ModuleController implements ICURDUniAbility {

    @Inject
    IDatabaseAccessUni databaseAccess;


    @Override
    public String getMainTable() {
        return "app_module";
    }

    @Override
    public IDatabaseAccessUni getDatabase() {
        return databaseAccess;
    }
}
