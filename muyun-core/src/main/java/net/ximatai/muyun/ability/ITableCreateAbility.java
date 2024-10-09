package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

/**
 * 创建表能力
 */
public interface ITableCreateAbility extends IMetadataAbility {

    /**
     * 装配表信息
     *
     * @param wrapper
     */
    void fitOut(TableWrapper wrapper);

    default void onTableCreated(boolean isFirst) {
    }

    @Transactional
    default void create(IDatabaseOperations db) {
        TableWrapper wrapper = TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName());

        fitOut(wrapper);

        if (this instanceof ISoftDeleteAbility ability) {
            wrapper.addColumn(ability.getSoftDeleteColumn());
            if (wrapper.getInherits().contains(BaseBusinessTable.TABLE)) {
                wrapper.addColumn("t_delete");
                wrapper.addColumn("id_at_auth_user__delete");
            }
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


