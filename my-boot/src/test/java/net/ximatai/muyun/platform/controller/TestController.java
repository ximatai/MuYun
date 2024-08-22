package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.database.IDatabaseAccess;

@Path("/test")
public class TestController implements ICURDAbility {

    @Inject
    IDatabaseAccess databaseAccess;

    @Override
    public String getMainTable() {
        return "test_table";
    }

    @Override
    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }
}
