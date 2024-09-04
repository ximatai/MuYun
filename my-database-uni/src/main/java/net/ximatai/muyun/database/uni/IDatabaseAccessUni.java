package net.ximatai.muyun.database.uni;

import io.smallrye.mutiny.Uni;
import net.ximatai.muyun.database.IDatabaseAccess;

import java.util.List;
import java.util.Map;

public interface IDatabaseAccessUni extends IDatabaseAccess {

    @Override
    default Uni<String> insertItem(String tableName, Map<String, ?> params) {
        return (Uni<String>) IDatabaseAccess.super.insertItem(tableName, params);
    }

    default Uni<Boolean> updateItem(String tableName, Map<String, ?> params) {
        Uni<Integer> updated = (Uni<Integer>) IDatabaseAccess.super.updateItem(tableName, params);
        return updated.onItem().transform(rowsUpdated -> rowsUpdated == 1);
    }

    <T> Uni<T> insert(String sql, Map<String, ?> params, String pk, Class<T> idType);

    Uni<Map<String, Object>> row(String sql, Map<String, ?> params);

    Uni<Map<String, Object>> row(String sql, List<?> params);

    Uni<Map<String, Object>> row(String sql);

    Uni<List<Map<String, Object>>> query(String sql, Map<String, ?> params);

    Uni<List<Map<String, Object>>> query(String sql, List<?> params);

    Uni<List<Map<String, Object>>> query(String sql);

    Uni<Integer> update(String sql, Map<String, ?> params);

    Uni<Integer> delete(String sql, Map<String, ?> params);

    Uni<Void> execute(String sql);

}
