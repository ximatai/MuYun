package net.ximatai.muyun.database.std;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseAccessStd;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccessStd implements IDatabaseAccessStd {

    @Inject
    Jdbi jdbi;

    //TODO 要审查params的内容类型，比如字符串的日期要做转化，才能入库
    @Override
    public String insert(String sql, Map<String, Object> params, String pk) {
        return row(sql, params).get(pk).toString();
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, Object> params) {
        try {
            return jdbi.withHandle(handle -> {
                var query = handle.createQuery(sql);
                if (params != null) {
                    params.forEach(query::bind);
                }

                return query.mapToMap().one();
            });
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
        return jdbi.withHandle(handle -> {
            var update = handle.createUpdate(sql);
            if (params != null) {
                params.forEach(update::bind);
            }
            return update.execute();
        });
    }

    @Override
    public Integer delete(String sql, Map<String, Object> params) {
        return update(sql, params);
    }
}
