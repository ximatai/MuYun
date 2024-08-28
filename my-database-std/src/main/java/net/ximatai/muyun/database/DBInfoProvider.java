package net.ximatai.muyun.database;

import jakarta.inject.Inject;
import net.ximatai.muyun.database.metadata.DBInfo;
import org.jdbi.v3.core.Jdbi;

public class DBInfoProvider implements IDBInfoProvider {

    private Jdbi jdbi;
    private DBInfo dbInfo;

    @Override
    public Jdbi getJdbi() {
        return jdbi;
    }

    @Inject
    @Override
    public void setJdbi(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Override
    public DBInfo getDBInfo() {
        if (dbInfo == null) {
            dbInfo = IDBInfoProvider.super.getDBInfo();
        }
        return dbInfo;
    }

}
