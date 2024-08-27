package net.ximatai.muyun.core;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.TableBuilder;

public class Scaffold {

    @Inject
    IDatabaseAccess databaseAccess;

    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }

    @PostConstruct
    void init() {
        if (this instanceof ITableCreateAbility) {
            new TableBuilder(databaseAccess)
                .build(((ITableCreateAbility) this).fitOutTable());
        }
    }

}
