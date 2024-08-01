package net.ximatai.muyun.database;

import io.smallrye.mutiny.Uni;

import java.util.List;
import java.util.Map;

public interface IDatabaseAccessUni extends IDatabaseAccess {

    Uni<String> insert(String sql, Map<String, Object> params);

    Uni<Map<String, Object>> row(String sql, Map<String, Object> params);

    Uni<Map<String, Object>> row(String sql);

    Uni<List<Map<String, Object>>> query(String sql, Map<String, Object> params);

    Uni<List<Map<String, Object>>> query(String sql);

    Uni<Integer> update(String sql, Map<String, Object> params);

    Uni<Integer> delete(String sql, Map<String, Object> params);

}
