package net.ximatai.muyun.database.standard;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.tool.TupleTool;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccess implements IDatabaseAccess {

    @Inject
    EntityManager entityManager;


    //TODO 要审查params的内容类型，比如字符串的日期要做转化，才能入库
    @Override
    public String insert(String sql, Map<String, Object> params) {
        return row(sql, params).get("id").toString();
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, Object> params) {
        try {
            Query query = entityManager.createNativeQuery(sql, Tuple.class);

            if (params != null) {
                params.forEach(query::setParameter);
            }

            Tuple tuple = (Tuple) query.getSingleResult();

            return TupleTool.toMap(tuple);
        } catch (Exception e) {
            throw new MyDatabaseException(e.getMessage(), MyDatabaseException.Type.DATA_NOT_FOUND);
        }

    }

    @Override
    public Map<String, Object> row(String sql) {
        return row(sql, null);
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
    @Transactional
    public Integer update(String sql, Map<String, Object> params) {
        Query query = entityManager.createNativeQuery(sql);

        if (params != null) {
            params.forEach(query::setParameter);
        }

        return query.executeUpdate();
    }

    @Override
    public Integer delete(String sql, Map<String, Object> params) {
        return update(sql, params);
    }
}
