package net.ximatai.muyun.database.plugin.mapper;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import org.jdbi.v3.core.mapper.MapMapper;
import org.jdbi.v3.core.mapper.MapMappers;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;
import org.postgresql.util.PGobject;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.UnaryOperator;

public class MyPgMapMapper extends MapMapper {

    private final Function<StatementContext, UnaryOperator<String>> caseStrategy;

    private Jsonb jsonb = JsonbBuilder.create();

    public MyPgMapMapper() {
        caseStrategy = ctx -> ctx.getConfig(MapMappers.class).getCaseChange();
    }

    @Override
    public RowMapper<Map<String, Object>> specialize(ResultSet rs, StatementContext ctx) throws SQLException {
        final List<String> columnNames = getColumnNames(rs, caseStrategy.apply(ctx));

        return (r, c) -> {
            Map<String, Object> row = new LinkedHashMap<>(columnNames.size());

            for (int i = 0; i < columnNames.size(); i++) {
                row.put(columnNames.get(i), pg2java(rs.getObject(i + 1)));
            }

            return row;
        };
    }

    private Object pg2java(Object pgObject) {
        if (pgObject == null) {
            return null;
        }

        if (pgObject instanceof Array array) {
            try {
                return array.getArray();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        } else if (pgObject instanceof PGobject val) {
            String value = val.getValue();
            String type = val.getType();
            if ("json".equals(type) || "jsonb".equals(type)) {
                if (value.trim().startsWith("[")) {
                    return jsonb.fromJson(value, List.class);
                } else {
                    return jsonb.fromJson(value, Map.class);
                }

            }
        }
        return pgObject;
    }

    private static List<String> getColumnNames(ResultSet rs, UnaryOperator<String> caseChange) throws SQLException {
        // important: ordered and unique
        Set<String> columnNames = new LinkedHashSet<>();
        ResultSetMetaData meta = rs.getMetaData();
        int columnCount = meta.getColumnCount();

        for (int i = 0; i < columnCount; i++) {
            String columnName = meta.getColumnName(i + 1);
            String alias = meta.getColumnLabel(i + 1);

            String name = caseChange.apply(alias == null ? columnName : alias);

            boolean added = columnNames.add(name);
            if (!added) {
                throw new RuntimeException("column " + name + " appeared twice in this resultset!");
            }
        }

        return new ArrayList<>(columnNames);
    }

}
