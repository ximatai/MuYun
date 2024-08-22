package net.ximatai.muyun.database;

import java.util.List;
import java.util.Map;

public interface IDatabaseAccessStd extends IDatabaseAccess {

    String create(String sql, Map<String, Object> params, String pk);

    Map<String, Object> row(String sql, Map<String, Object> params);

    Map<String, Object> row(String sql);

    List<Map<String, Object>> query(String sql, Map<String, Object> params);

    List<Map<String, Object>> query(String sql);

    Integer update(String sql, Map<String, Object> params);

    Integer delete(String sql, Map<String, Object> params);

}
