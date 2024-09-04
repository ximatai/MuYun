package net.ximatai.muyun.database.std;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.DBInfoProvider;
import net.ximatai.muyun.database.IDatabaseAccessStd;
import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DataAccessStd extends DBInfoProvider implements IDatabaseAccessStd {

    @Override
    public <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createUpdate(sql);
            if (params != null) {
                params.forEach(query::bind);
            }
            return query.executeAndReturnGeneratedKeys(pk).mapTo(idType).one();

        });
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, ?> params) {
        var row = getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);
            if (params != null) {
                params.forEach(query::bind);
            }

            return query.mapToMap().findOne().orElse(null);
        });

        if (row == null) {
            throw new MyDatabaseException(null, MyDatabaseException.Type.DATA_NOT_FOUND);
        }

        return row;
    }

    @Override
    public Map<String, Object> row(String sql, List<?> params) {
        var row = getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.mapToMap().findOne().orElse(null);
        });

        if (row == null) {
            throw new MyDatabaseException(null, MyDatabaseException.Type.DATA_NOT_FOUND);
        }

        return row;
    }

    @Override
    public Map<String, Object> row(String sql) {
        return row(sql, Collections.emptyList());
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);
            if (params != null) {
                params.forEach(query::bind);
            }

            return query.mapToMap().list();
        });
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.mapToMap().list();
        });
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        return List.of();
    }

    @Override
    @Transactional
    public Integer update(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> {
            var update = handle.createUpdate(sql);
            if (params != null) {
                params.forEach(update::bind);
            }
            return update.execute();
        });
    }

    @Override
    public Integer delete(String sql, Map<String, ?> params) {
        return update(sql, params);
    }

    @Override
    public Object execute(String sql) {
        getJdbi().withHandle(handle -> handle.execute(sql));
        return null;
    }
}
