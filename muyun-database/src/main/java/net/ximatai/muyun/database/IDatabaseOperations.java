package net.ximatai.muyun.database;

import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBTable;

import java.sql.Array;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

import static net.ximatai.muyun.util.PreconditionUtil.require;

public interface IDatabaseOperations extends IDBInfoProvider {

    default Map<String, ?> transformDataForDB(DBTable dbTable, Map<String, ?> data) {
        return data;
    }

    default String buildInsertSql(String schema, String tableName, Map<String, ?> params) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
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

        return "insert into %s.%s %s values %s".formatted(schema, tableName, columns, values);
    }

    default String buildUpdateSql(String schema, String tableName, Map<String, ?> params, String pk) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner setClause = new StringJoiner(", ");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                setClause.add(key + "=:" + key);
            }
        });

        return "update %s.%s set %s where %s = :%s".formatted(schema, tableName, setClause, pk, pk);
    }

    default Object insertItem(String schema, String tableName, Map<String, ?> params) {
        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        Map<String, ?> transformed = transformDataForDB(table, params);
        return this.insert(buildInsertSql(schema, tableName, transformed), transformed, "id", String.class);
    }

    default Object insertList(String schema, String tableName, List<Map> list) {
        Objects.requireNonNull(list, "The list must not be null");
        require(!list.isEmpty(), () -> "The list must not be empty");

        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        List<Map> transformedList = list.stream().map(it -> transformDataForDB(table, it)).toList();

        return this.batchInsert(buildInsertSql(schema, tableName, transformedList.getFirst()), transformedList, "id", String.class);
    }

    default Object updateItem(String schema, String tableName, Map<String, ?> params) {
        DBTable table = getDBInfo().getSchema(schema).getTable(tableName);
        Map<String, ?> transformed = transformDataForDB(table, params);
        return this.update(buildUpdateSql(schema, tableName, transformed, "id"), transformed);
    }

    default Object deleteItem(String schema, String tableName, String id) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        return this.delete("DELETE FROM %s.%s WHERE id=:id".formatted(schema, tableName), Map.of("id", id));
    }

    default Object getItem(String schema, String tableName, String id) {
        DBTable dbTable = getDBInfo().getSchema(schema).getTable(tableName);
        Objects.requireNonNull(dbTable);

        return this.row("SELECT * FROM %s.%s WHERE id=:id".formatted(schema, tableName), Map.of("id", id));
    }

    <T> Object insert(String sql, Map<String, ?> params, String pk, Class<T> idType);

    <T> Object batchInsert(String sql, List<Map> paramsList, String pk, Class<T> idType);

    Object row(String sql, Object... params);

    Object row(String sql, List<?> params);

    Object row(String sql, Map<String, ?> params);

    Object row(String sql);

    Object query(String sql, Map<String, ?> params);

    Object query(String sql, List<?> params);

    Object query(String sql, Object... params);

    Object query(String sql);

    Object update(String sql, Map<String, ?> params);

    Object update(String sql, Object... params);

    Object delete(String sql, Map<String, ?> params);

    Object delete(String sql, Object... params);

    Object delete(String sql, List<?> params);

    Object execute(String sql);

    Object execute(String sql, Object... params);

    Object execute(String sql, List<?> params);

    Array createArray(List list, String type);
}
