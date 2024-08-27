package net.ximatai.muyun.database;

import net.ximatai.muyun.database.metadata.DBInfo;
import org.jdbi.v3.core.Jdbi;

import java.util.Map;

public interface IDatabaseAccess {

    Jdbi getJdbi();

    DBInfo getDBInfo();

    Object insert(String sql, Map<String, Object> params);

    Object create(String sql, Map<String, Object> params, String pk);

    Object row(String sql, Map<String, Object> params);

    Object row(String sql);

    Object query(String sql, Map<String, Object> params);

    Object query(String sql);

    Object update(String sql, Map<String, Object> params);

    Object delete(String sql, Map<String, Object> params);

    Object execute(String sql);
}
