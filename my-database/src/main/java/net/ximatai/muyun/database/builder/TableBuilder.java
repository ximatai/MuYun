package net.ximatai.muyun.database.builder;

import org.jdbi.v3.core.Jdbi;

public class TableBuilder {

    Jdbi jdbi;

    public TableBuilder(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    String generateSQL(TableWrapper wrapper) {

        return "";

    }

}
