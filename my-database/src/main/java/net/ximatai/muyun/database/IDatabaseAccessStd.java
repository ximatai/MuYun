package net.ximatai.muyun.database;

import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.List;
import java.util.Map;

public interface IDatabaseAccessStd extends IDatabaseAccess {

    default String insertItem(String tableName, Map<String, Object> params) {
        return (String) IDatabaseAccess.super.insertItem(tableName, params);
    }

    default Integer updateItem(String tableName, Map<String, Object> params) {
        Integer num = (Integer) IDatabaseAccess.super.updateItem(tableName, params);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    default Integer deleteItem(String tableName, String id) {
        Integer num = (Integer) IDatabaseAccess.super.deleteItem(tableName, id);
        if (num == 0) {
            throw new MyDatabaseException(MyDatabaseException.Type.DATA_NOT_FOUND);
        }
        return num;
    }

    <T> T insert(String sql, Map<String, Object> params, String pk, Class<T> idType);

    Map<String, Object> row(String sql, Map<String, Object> params);

    Map<String, Object> row(String sql);

    List<Map<String, Object>> query(String sql, Map<String, Object> params);

    List<Map<String, Object>> query(String sql);

    Integer update(String sql, Map<String, Object> params);

    Integer delete(String sql, Map<String, Object> params);

}
