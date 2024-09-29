package net.ximatai.muyun.database.std.mapper;

import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

public class PgArrayToListMapper implements ColumnMapper<List<Object>> {

    @Override
    public List<Object> map(ResultSet r, int columnNumber, StatementContext ctx) throws SQLException {
        Array pgArray = r.getArray(columnNumber);

        if (pgArray == null) {
            return null;
        }

        // 处理结果集中的数组
        Object array = pgArray.getArray();
        if (array instanceof Object[]) {
            return Arrays.asList((Object[]) array); // 转换为 List<Object>
        } else if (array instanceof int[]) {
            return Arrays.asList(Arrays.stream((int[]) array).boxed().toArray());
        } else if (array instanceof long[]) {
            return Arrays.asList(Arrays.stream((long[]) array).boxed().toArray());
        } else if (array instanceof double[]) {
            return Arrays.asList(Arrays.stream((double[]) array).boxed().toArray());
        } else {
            throw new SQLException("Unsupported array type: " + array.getClass().getName());
        }
    }
}
