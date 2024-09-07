package net.ximatai.muyun.database.std.mapper;

import org.jdbi.v3.core.config.ConfigRegistry;
import org.jdbi.v3.core.mapper.ColumnMapper;
import org.jdbi.v3.core.mapper.ColumnMapperFactory;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.sql.Array;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class PgArrayToListMapperFactory implements ColumnMapperFactory {

    @Override
    public Optional<ColumnMapper<?>> build(Type type, ConfigRegistry config) {
        // 检查类型是否是泛型，并且是否是 List 类型
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() == List.class) {
                return Optional.of((rs, columnNumber, ctx) -> {
                    Array pgArray = rs.getArray(columnNumber);

                    if (pgArray == null) {
                        return null;
                    }

                    Object array = pgArray.getArray();
                    if (array instanceof Object[]) {
                        return Arrays.asList((Object[]) array); // 转换为 List
                    } else if (array instanceof int[] || array instanceof double[] || array instanceof float[]) {
                        // 处理其他原生类型数组
                        Object[] objectArray = (Object[]) pgArray.getArray();
                        return Arrays.asList(objectArray);
                    } else {
                        throw new SQLException("Unsupported array type: " + array.getClass().getName());
                    }
                });
            }
        }
        return Optional.empty();
    }
}
