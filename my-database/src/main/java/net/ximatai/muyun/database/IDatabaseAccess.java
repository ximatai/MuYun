package net.ximatai.muyun.database;

import java.util.Map;

public interface IDatabaseAccess extends IDBInfoProvider {

    Object insertItem(String table, Map<String, Object> params);

    Object updateItem(String table, Map<String, Object> params);

    void insert(String sql, Map<String, Object> params);

    <T> Object insert(String sql, Map<String, Object> params, String pk, Class<T> idType);

    Object row(String sql, Map<String, Object> params);

    Object row(String sql);

    Object query(String sql, Map<String, Object> params);

    Object query(String sql);

    Object update(String sql, Map<String, Object> params);

    Object delete(String sql, Map<String, Object> params);

    Object execute(String sql);
}
