package net.ximatai.muyun.ability;

import jakarta.annotation.PostConstruct;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

public interface ITableCreateAbility {

    TableWrapper fitOutTable();

    @PostConstruct
    default void create(IDatabaseAccess databaseAccess) {
        TableWrapper wrapper = fitOutTable();
        if (this instanceof ICommonBusinessAbility commonBusinessAbility) {
            commonBusinessAbility.getCommonColumns().forEach(wrapper::addColumn);
        }
        if (this instanceof ISoftDeleteAbility softDeleteAbility) {
            wrapper.addColumn(softDeleteAbility.getSoftDeleteColumn());
        }
        new TableBuilder(databaseAccess).build(wrapper);
    }
}


