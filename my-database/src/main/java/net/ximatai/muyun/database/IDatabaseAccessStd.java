package net.ximatai.muyun.database;

import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface IDatabaseAccessStd extends IDatabaseAccess {

    default String insertItem(String schema, String tableName, Map<String, ?> params) {
        return (String) IDatabaseAccess.super.insertItem(schema, tableName, params);
    }

    default Integer updateItem(String schema, String tableName, Map<String, ?> params) {
        Integer num = (Integer) IDatabaseAccess.super.updateItem(schema, tableName, params);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    default Integer deleteItem(String schema, String tableName, String id) {
        Integer num = (Integer) IDatabaseAccess.super.deleteItem(schema, tableName, id);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType);

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

    Integer delete(String sql, Map<String, ?> params);

}
