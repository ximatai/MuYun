package net.ximatai.muyun.database.standard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.tool.TupleTool;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccess implements IDatabaseAccess {

    @Inject
    EntityManager entityManager;

    @Override
    public String insert(String sql, Map<String, Object> params) {
        return "";
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql, Tuple.class);

        if (params != null) {
            params.forEach(query::setParameter);
        }

        Tuple tuple = (Tuple) query.getSingleResult();

        return TupleTool.toMap(tuple);

    }

    @Override
    public Map<String, Object> row(String sql) {
        return Map.of();
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, Object> params) {
        return List.of();
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        return List.of();
    }

    @Override
    public Integer update(String sql, Map<String, Object> params) {
        return 0;
    }

    @Override
    public Integer delete(String sql, Map<String, Object> params) {
        return 0;
    }
}
