package net.ximatai.muyun.database;

import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBTable;

import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

public interface IDatabaseAccess extends IDBInfoProvider {

    default String buildInsertSql(String tableName, Map<String, Object> params) {
        DBTable dbTable = getDBInfo().getTables().get(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                columns.add(key);
                values.add(":" + key);
            }
        });

        return "INSERT INTO " + tableName + " " + columns + " VALUES " + values;
    }

    default String buildUpdateSql(String tableName, Map<String, Object> params, String pk) {
        DBTable dbTable = getDBInfo().getTables().get(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner setClause = new StringJoiner(", ");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                setClause.add(key + "=:" + key);
            }
        });

        return "UPDATE " + tableName + " SET " + setClause + " WHERE " + pk + "=:" + pk;
    }

    default Object insertItem(String tableName, Map<String, Object> params) {
        return this.insert(buildInsertSql(tableName, params), params, "id", String.class);
    }

    default Object updateItem(String tableName, Map<String, Object> params) {
        return this.update(buildUpdateSql(tableName, params, "id"), params);
    }

    default Object deleteItem(String tableName, String id) {
        DBTable dbTable = getDBInfo().getTables().get(tableName);
        Objects.requireNonNull(dbTable);

        return this.delete("DELETE FROM " + tableName + " WHERE id=:id", Map.of("id", id));
    }

    <T> Object insert(String sql, Map<String, Object> params, String pk, Class<T> idType);

    Object row(String sql, Map<String, Object> params);

    Object row(String sql);

    Object query(String sql, Map<String, Object> params);

    Object query(String sql);

    Object update(String sql, Map<String, Object> params);

    Object delete(String sql, Map<String, Object> params);

    Object execute(String sql);
}
