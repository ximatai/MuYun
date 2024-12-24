package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import net.ximatai.muyun.core.exception.MuYunException;
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
        String mainTable = getMainTable();
        if (!mainTable.toLowerCase().equals(mainTable)) {
            throw new MuYunException("%s表名不合法，不允许使用大写字母".formatted(mainTable));
        }

        TableWrapper wrapper = TableWrapper.withName(mainTable)
            .setSchema(getSchemaName());

        fitOut(wrapper);

        if (this instanceof ITreeAbility ability) {
            wrapper.addColumn(ability.getParentKeyColumn());
        }
        if (this instanceof ISortAbility ability) {
            wrapper.addColumn(ability.getSortColumn().getColumn());
        }
        if (this instanceof ISecurityAbility ability) {
            ability.getSignColumns().forEach(wrapper::addColumn);
        }

        if (this instanceof ISoftDeleteAbility ability) {
            if (this instanceof IArchiveWhenDelete) {
                throw new MuYunException("ISoftDeleteAbility 能力与 IArchiveWhenDelete 能力互斥，不可同时采用");
            }

            wrapper.addColumn(ability.getSoftDeleteColumn());
            wrapper.addColumn("t_delete");
            wrapper.addColumn("id_at_auth_user__delete");
        }

        boolean build = new TableBuilder(db).build(wrapper);

        if (this instanceof IArchiveWhenDelete ability) {
            wrapper.setName(ability.getArchiveTableName());
            wrapper.addColumn("t_archive", "归档时间", "now()");
            wrapper.addColumn("id_at_auth_user__archive", "归档人");
            new TableBuilder(db).build(wrapper);
        }

        this.onTableCreated(build);
    }

}


