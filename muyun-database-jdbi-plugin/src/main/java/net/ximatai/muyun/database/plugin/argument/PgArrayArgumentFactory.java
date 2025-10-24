package net.ximatai.muyun.database.plugin.argument;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.postgresql.jdbc.PgArray;

import java.sql.SQLException;
import java.sql.Types;

public class PgArrayArgumentFactory extends AbstractArgumentFactory<PgArray> {

    public PgArrayArgumentFactory() {
        super(Types.ARRAY); // 指定数据库类型为 ARRAY
    }

    @Override
    protected Argument build(PgArray value, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            try {
                statement.setArray(position, value);
            } catch (SQLException e) {
                throw new RuntimeException("Error setting PgArray argument", e);
            }
        };
    }
}
