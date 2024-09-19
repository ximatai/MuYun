package net.ximatai.muyun.ability;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

public interface ITableCreateAbility {

    TableWrapper fitOutTable();

    @Transactional
    @PostConstruct
    default void create(IDatabaseOperations db) {
        TableWrapper wrapper = fitOutTable();
        if (this instanceof ICommonBusinessAbility ability) {
            ability.getCommonColumns().forEach(wrapper::addColumn);
        }
        if (this instanceof ISoftDeleteAbility ability) {
            wrapper.addColumn(ability.getSoftDeleteColumn());
        }
        if (this instanceof ITreeAbility ability) {
            wrapper.addColumn(ability.getParentKeyColumn());
        }
        if (this instanceof ISortAbility ability) {
            wrapper.addColumn(ability.getSortColumn().getColumn());
        }
        if (this instanceof ISecurityAbility ability) {
            ability.getSignColumns().forEach(wrapper::addColumn);
        }

        new TableBuilder(db).build(wrapper);
    }
}


