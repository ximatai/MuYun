package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

public interface ITableCreateAbility {

    TableWrapper getTableWrapper();

    default void onTableCreated(boolean isFirst) {
    }

    @Transactional
    default void create(IDatabaseOperations db) {
        TableWrapper wrapper = getTableWrapper();
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

        boolean build = new TableBuilder(db).build(wrapper);
        this.onTableCreated(build);
    }

}


