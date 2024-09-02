package net.ximatai.muyun.database.uni;

import io.smallrye.mutiny.Uni;
import net.ximatai.muyun.database.IDatabaseAccess;

import java.util.List;
import java.util.Map;

public interface IDatabaseAccessUni extends IDatabaseAccess {

    Uni<String> insertItem(String table, Map<String, Object> params);

    <T> Uni<T> insert(String sql, Map<String, Object> params, String pk, Class<T> idType);

    Uni<Map<String, Object>> row(String sql, Map<String, Object> params);

    Uni<Map<String, Object>> row(String sql);

    Uni<List<Map<String, Object>>> query(String sql, Map<String, Object> params);

    Uni<List<Map<String, Object>>> query(String sql);

    Uni<Integer> update(String sql, Map<String, Object> params);

    Uni<Integer> delete(String sql, Map<String, Object> params);

    Uni<Void> execute(String sql);

}
