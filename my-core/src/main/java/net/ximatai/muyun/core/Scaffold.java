package net.ximatai.muyun.core;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

public class Scaffold {

    @Inject
    IDatabaseAccess databaseAccess;

    public IDatabaseAccess getDatabaseAccess() {
        return databaseAccess;
    }

    @PostConstruct
    void init() {
        if (this instanceof ITableCreateAbility tableCreateAbility) {
            TableWrapper wrapper = tableCreateAbility.fitOutTable();
            if (this instanceof ISoftDeleteAbility softDeleteAbility) {
                wrapper.addColumn(softDeleteAbility.getSoftDeleteColumn());
            }
            new TableBuilder(databaseAccess).build(wrapper);
        }
    }

}
