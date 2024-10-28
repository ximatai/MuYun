package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.builder.TableBase;
import net.ximatai.muyun.database.metadata.DBTable;

/**
 * 元数据信息能力
 */
public interface IMetadataAbility extends IDatabaseAbility {

    String getMainTable();

    default String getSchemaName() {
        return "public";
    }

    default TableBase getTableBase() {
        return new TableBase(getSchemaName(), getMainTable());
    }

    default String getPK() {
        return "id";
    }

    default DBTable getDBTable() {
        return getDatabaseOperations().getDBInfo().getSchema(getSchemaName()).getTable(getMainTable());
    }

    default boolean checkColumn(String column) {
        return getDBTable().contains(column);
    }

}
