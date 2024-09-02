package net.ximatai.muyun.database.std;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.DBInfoProvider;
import net.ximatai.muyun.database.IDatabaseAccessStd;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBTable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.StringJoiner;

@ApplicationScoped
public class DataAccessStd extends DBInfoProvider implements IDatabaseAccessStd {

    @Override
    public void insert(String sql, Map<String, Object> params) {
        update(sql, params);
    }

    //TODO 要审查params的内容类型，比如字符串的日期要做转化，才能入库
    @Override
    public String insertItem(String tableName, Map<String, Object> params) {
        DBTable dbTable = getDBInfo().getTables().get(tableName);
        Objects.requireNonNull(dbTable);

        Map<String, DBColumn> columnMap = dbTable.getColumnMap();

        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");
        params.keySet().forEach(key -> {
            if (columnMap.containsKey(key)) {
                columns.add(key);
                values.add(":" + key);
            }
        });

        String sql = "INSERT INTO " + tableName + " " + columns + " VALUES " + values;

        return this.insert(sql, params, "id", String.class);
    }

    @Override
    public Boolean updateItem(String table, Map<String, Object> params) {
        return null;
    }

    @Override
    public <T> T insert(String sql, Map<String, Object> params, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createUpdate(sql);
            if (params != null) {
                params.forEach(query::bind);
            }
            return query.executeAndReturnGeneratedKeys(pk).mapTo(idType).one();

        });
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, Object> params) {
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
        return getJdbi().withHandle(handle -> {
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

    @Override
    public Object execute(String sql) {
        getJdbi().withHandle(handle -> handle.execute(sql));
        return null;
    }
}
