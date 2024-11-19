package net.ximatai.muyun.ability.curd.std;

import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBTable;
import net.ximatai.muyun.model.CheckConfig;

import java.util.List;
import java.util.Map;

/**
 * 数据新增、修改时进行校验的能力
 */
public interface IDataCheckAbility extends IMetadataAbility {

    default CheckConfig getCheckConfig() {
        CheckConfig config = new CheckConfig();
        DBTable dbTable = getDBTable();
        dbTable.getColumnMap().forEach((k, v) -> {
            if (v.isPrimaryKey()) {
                return;
            }
            if (!v.isNullable()) {
                config.addNonEmpty(v.getName(), "数据项[%s]要求为必填".formatted(v.getLabel()));
            }
        });

        dbTable.getIndexList().forEach(dbIndex -> {
            if (dbIndex.isUnique() && !dbIndex.isMulti()) {
                String columnName = dbIndex.getColumns().getFirst();
                DBColumn column = dbTable.getColumn(columnName);
                config.addUnique(columnName, "数据项[%s]已存在相同的数据".formatted(column.getLabel()));
            }

        });

        fitOut(config);

        return config;
    }

    default void fitOut(CheckConfig config) {
    }

    default void check(Map body, boolean isUpdate) {

    }

    default void checkWhenCreate(Map body) {
        CheckConfig config = getCheckConfig();
        config.getNonEmptyMap().forEach((column, tip) -> {
            checkColumn(body.get(column), tip);
        });

        config.getUniqueMap().forEach((column, tip) -> {
            if (body.get(column) != null) {
                String deleteWhere = "";
                if (this instanceof ISoftDeleteAbility ability) {
                    deleteWhere += " AND %s = false ".formatted(ability.getSoftDeleteColumn());
                }
                Object row = getDatabaseOperations().row("select 1 from %s.%s where %s = ? %s"
                    .formatted(getSchemaName(), getMainTable(), column, deleteWhere), body.get(column));
                if (row != null) {
                    throw new MyException(tip);
                }
            }
        });

    }

    default void checkWhenUpdate(String id, Map body) {
        CheckConfig config = getCheckConfig();
        config.getNonEmptyMap().forEach((column, tip) -> {
            if (body.containsKey(column)) {
                checkColumn(body.get(column), tip);
            }

        });

        config.getUniqueMap().forEach((column, tip) -> {
            if (body.get(column) != null) {
                String deleteWhere = "";
                if (this instanceof ISoftDeleteAbility ability) {
                    deleteWhere += " and %s = false ".formatted(ability.getSoftDeleteColumn());
                }
                Object row = getDatabaseOperations().row("select 1 from %s.%s where %s = ? %s and id != ?"
                    .formatted(getSchemaName(), getMainTable(), column, deleteWhere), body.get(column), id);
                if (row != null) {
                    throw new MyException(tip);
                }
            }
        });
    }

    private void checkColumn(Object field, String tip) {
        if (field == null
            || (field instanceof String str && str.isBlank())
            || (field instanceof Map<?, ?> map && map.isEmpty())
            || (field instanceof List<?> list && list.isEmpty())
            || (field instanceof Object[] arr && arr.length == 0)) {
            throw new MyException(tip);
        }
    }

}
