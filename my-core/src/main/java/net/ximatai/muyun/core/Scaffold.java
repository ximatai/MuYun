package net.ximatai.muyun.core;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseAccess;

public class Scaffold {

    private IDatabaseAccess databaseAccess;

    @Inject
    public void setDatabaseAccess(IDatabaseAccess databaseAccess) {
        this.databaseAccess = databaseAccess;
    }

    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }

    @PostConstruct
    void init() {
        if (this instanceof ITableCreateAbility tableCreateAbility) {
            tableCreateAbility.create(getDatabaseAccess());
        }
    }

}
