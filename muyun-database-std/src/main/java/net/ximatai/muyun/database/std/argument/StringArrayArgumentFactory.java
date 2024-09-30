package net.ximatai.muyun.database.std.argument;

import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;

import java.sql.Array;
import java.sql.SQLException;
import java.sql.Types;

public class StringArrayArgumentFactory extends AbstractArgumentFactory<String[]> {

    public StringArrayArgumentFactory() {
        super(Types.ARRAY); // 指定数据库类型为 ARRAY
    }

    @Override
    protected Argument build(String[] value, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            try {
                Array array = statement.getConnection().createArrayOf("VARCHAR", value);
                statement.setArray(position, array);
            } catch (SQLException e) {
                throw new RuntimeException("Error setting PgArray argument", e);
            }
        };
    }
}
