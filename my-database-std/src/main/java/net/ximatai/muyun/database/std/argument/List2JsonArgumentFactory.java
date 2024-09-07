package net.ximatai.muyun.database.std.argument;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.jdbi.v3.core.argument.AbstractArgumentFactory;
import org.jdbi.v3.core.argument.Argument;
import org.jdbi.v3.core.config.ConfigRegistry;
import org.postgresql.util.PGobject;

import java.sql.SQLException;
import java.sql.Types;
import java.util.List;

public class List2JsonArgumentFactory extends AbstractArgumentFactory<List> {

    Jsonb jsonb = JsonbBuilder.create();

    public List2JsonArgumentFactory() {
        super(Types.OTHER);
    }

    @Override
    protected Argument build(List value, ConfigRegistry config) {
        return (position, statement, ctx) -> {
            try {
                PGobject jsonObject = new PGobject();
                jsonObject.setType("json");
                jsonObject.setValue(jsonb.toJson(value));
                statement.setObject(position, jsonObject);
            } catch (SQLException e) {
                throw new RuntimeException("Error setting PgArray argument", e);
            }
        };
    }
}
