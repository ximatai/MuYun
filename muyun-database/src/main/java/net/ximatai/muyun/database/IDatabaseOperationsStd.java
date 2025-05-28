package net.ximatai.muyun.database;

import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface IDatabaseOperationsStd extends IDatabaseOperations {

    default String insertItem(String schema, String tableName, Map<String, ?> params) {
        return (String) IDatabaseOperations.super.insertItem(schema, tableName, params);
    }

    default List<String> insertList(String schema, String tableName, List<Map> list) {
        return (List<String>) IDatabaseOperations.super.insertList(schema, tableName, list);
    }

    default Integer updateItem(String schema, String tableName, Map<String, ?> params) {
        Integer num = (Integer) IDatabaseOperations.super.updateItem(schema, tableName, params);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    default Map<String, Object> getItem(String schema, String tableName, String id) {
        return (Map<String, Object>) IDatabaseOperations.super.getItem(schema, tableName, id);
    }

    default Integer deleteItem(String schema, String tableName, String id) {
        Integer num = (Integer) IDatabaseOperations.super.deleteItem(schema, tableName, id);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType);

    <T> List<T> batchInsert(String sql, List<Map> paramsList, String pk, Class<T> idType);

    Map<String, Object> row(String sql, List<?> params);

    default Map<String, Object> row(String sql, Object... params) {
        return this.row(sql, Arrays.stream(params).toList());
    }

    Map<String, Object> row(String sql, Map<String, ?> params);

    Map<String, Object> row(String sql);

    List<Map<String, Object>> query(String sql, Map<String, ?> params);

    List<Map<String, Object>> query(String sql, List<?> params);

    default List<Map<String, Object>> query(String sql, Object... params) {
        return this.query(sql, Arrays.stream(params).toList());
    }

    List<Map<String, Object>> query(String sql);

    Integer update(String sql, Map<String, ?> params);

    default Integer update(String sql, Object... params) {
        return this.update(sql, Arrays.stream(params).toList());
    }

    Integer update(String sql, List<?> params);

    default Integer delete(String sql, Map<String, ?> params) {
        return this.update(sql, params);
    }

    default Integer delete(String sql, Object... params) {
        return this.update(sql, params);
    }

    default Integer delete(String sql, List<?> params) {
        return this.update(sql, params);
    }

}
