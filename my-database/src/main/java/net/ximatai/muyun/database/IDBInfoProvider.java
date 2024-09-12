package net.ximatai.muyun.database;

import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBInfo;
import net.ximatai.muyun.database.metadata.DBSchema;
import net.ximatai.muyun.database.metadata.DBTable;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

import static net.ximatai.muyun.database.exception.MyDatabaseException.Type.READ_METADATA_ERROR;

public interface IDBInfoProvider {

    Jdbi getJdbi();

    void setJdbi(Jdbi jdbi);

    void resetDBInfo();

    default DBInfo getDBInfo() {
        return getJdbi().withHandle(handle -> {
            Connection connection = handle.getConnection();
            try {
                DatabaseMetaData metaData = connection.getMetaData();

                DBInfo info = new DBInfo(metaData.getDatabaseProductName());

                try (ResultSet schemasRs = metaData.getSchemas()) {
                    while (schemasRs.next()) {
                        info.addSchema(new DBSchema(schemasRs.getString("TABLE_SCHEM")));
                    }
                }

                try (ResultSet tablesRs = metaData.getTables(null, null, null, new String[]{"TABLE"})) {
                    while (tablesRs.next()) {
                        String tableName = tablesRs.getString("TABLE_NAME");
                        String schema = tablesRs.getString("TABLE_SCHEM");
                        DBTable table = new DBTable(getJdbi()).setName(tableName).setSchema(schema);
                        info.getSchema(schema).addTable(table);
                    }
                }

                return info;
            } catch (Exception e) {
                throw new MyDatabaseException(e.getMessage(), READ_METADATA_ERROR);
            }
        });
    }

}
