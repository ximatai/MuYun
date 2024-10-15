package net.ximatai.muyun.database.std;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBTable;
import net.ximatai.muyun.database.std.mapper.MyPgMapMapper;
import net.ximatai.muyun.database.tool.DateTool;
import org.jdbi.v3.core.statement.PreparedBatch;
import org.postgresql.util.PGobject;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Array;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class DataAccessStd extends DBInfoProvider implements IDatabaseOperationsStd {

    Jsonb jsonb = JsonbBuilder.create();

    @Override
    public Map<String, ?> transformDataForDB(DBTable dbTable, Map<String, ?> data) {
        // 创建一个新的 Map 来存储修改后的数据
        Map<String, Object> transformedData = new HashMap<>(data);

        // 遍历原数据并修改
        transformedData.forEach((k, v) -> {
            DBColumn dbColumn = dbTable.getColumn(k);
            if (dbColumn != null) {
                transformedData.put(k, getDBValue(v, dbColumn.getType()));
            }
        });

        return transformedData;
    }

    public Object getDBValue(Object value, String type) {
        if (value == null) {
            return null;
        }

//        if (value instanceof List) {
//            return ((List<?>) value).toArray();
//        }

        // 以_开头的是数组类型，形如：_int4，目前只考虑支持 varchar、integer 和 bool
        if (type.startsWith("_")) {
            if (value instanceof java.sql.Array) {
                return value; // 已经是数组类型，直接返回
            }

            Object[] arrayValue;
            if (value instanceof String string) {
                arrayValue = string.split(",");
            } else if (value instanceof List<?> list) {
                arrayValue = list.toArray();
            } else {
                return value; // 不支持的输入类型，直接返回
            }

            String subType = type.substring(1);

            return switch (subType) {
                case "varchar" -> Arrays.stream(arrayValue)
                    .map(Object::toString)
                    .toArray(String[]::new);
                case "int4" -> Arrays.stream(arrayValue)
                    .map(val -> Integer.parseInt(val.toString()))
                    .toArray(Integer[]::new);
                case "bool" -> Arrays.stream(arrayValue)
                    .map(val -> Boolean.parseBoolean(val.toString()))
                    .toArray(Boolean[]::new);
                default -> value; // 不支持的类型，返回原值
            };
        }

        return switch (type) {
            case "varchar" -> value.toString();
            case "int8" -> convertToBigInteger(value);
            case "int4", "int2" -> convertToInteger(value);
            case "bool" -> isTrue(value);
            case "date", "timestamp" -> DateTool.handleDateTimestamp(value);
            case "numeric" -> convertToBigDecimal(value);
//            case "json", "jsonb" -> convertToJson(value);
            case "bytea" -> convertToByteArray(value);
            default -> value;
        };
    }

    @Override
    public <T> T insert(String sql, Map<String, ?> params, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> handle.createUpdate(sql)
            .bindMap(params)
            .executeAndReturnGeneratedKeys(pk).mapTo(idType).one());
    }

    @Override
    public <T> List<T> batchInsert(String sql, List<? extends Map<String, ?>> paramsList, String pk, Class<T> idType) {
        return getJdbi().withHandle(handle -> {
            List<T> generatedKeys = new ArrayList<>();
            PreparedBatch batch = handle.prepareBatch(sql);

            for (Map<String, ?> params : paramsList) {
                batch.bindMap(params).add();
            }

            batch.executePreparedBatch(pk)
                .mapTo(idType)
                .forEach(generatedKeys::add);

            return generatedKeys;
        });
    }

    @Override
    public Map<String, Object> row(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> handle.createQuery(sql)
            .bindMap(params)
            .map(new MyPgMapMapper())
            .findOne().orElse(null));
    }

    @Override
    public Map<String, Object> row(String sql, List<?> params) {
        var row = getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);
            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.map(new MyPgMapMapper()).findOne().orElse(null);
        });

        return row;
    }

    @Override
    public Map<String, Object> row(String sql) {
        return row(sql, Collections.emptyList());
    }

    @Override
    public List<Map<String, Object>> query(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> handle.createQuery(sql)
            .bindMap(params)
            .map(new MyPgMapMapper())
            .list());
    }

    @Override
    public List<Map<String, Object>> query(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createQuery(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.map(new MyPgMapMapper()).list();
        });
    }

    @Override
    public List<Map<String, Object>> query(String sql) {
        return this.query(sql, Collections.emptyList());
    }

    @Override
    @Transactional
    public Integer update(String sql, Map<String, ?> params) {
        return getJdbi().withHandle(handle -> handle.createUpdate(sql)
            .bindMap(params)
            .execute());
    }

    @Override
    @Transactional
    public Integer update(String sql, List<?> params) {
        return getJdbi().withHandle(handle -> {
            var query = handle.createUpdate(sql);

            if (params != null && !params.isEmpty()) {
                for (int i = 0; i < params.size(); i++) {
                    query.bind(i, params.get(i));  // 通过索引绑定参数
                }
            }

            return query.execute();
        });
    }

    @Override
    public Object execute(String sql) {
        getJdbi().withHandle(handle -> handle.execute(sql));
        return null;
    }

    public Array toArray(List list, String type) {
        try {
            return getJdbi().withHandle(handle -> {
                Connection connection = handle.getConnection();
                return connection.createArrayOf(type, list.toArray());
            });
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private BigInteger convertToBigInteger(Object value) {
        if (value instanceof String) {
            return new BigInteger((String) value);
        } else if (value instanceof Number) {
            return BigInteger.valueOf(((Number) value).longValue());
        }
        throw new IllegalArgumentException("Cannot convert to BigInteger: " + value);
    }

    private Integer convertToInteger(Object value) {
        if (value instanceof String) {
            return Integer.valueOf((String) value);
        } else if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        throw new IllegalArgumentException("Cannot convert to Integer: " + value);
    }

    private BigDecimal convertToBigDecimal(Object value) {
        if (value instanceof String && !isBlank((String) value)) {
            return new BigDecimal((String) value);
        } else if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        return null;
    }

    private byte[] convertToByteArray(Object value) {
        if (value instanceof byte[]) {
            return (byte[]) value;
        }
        return value.toString().getBytes();
    }

    private PGobject convertToJson(Object value) {
        try {
            PGobject jsonObject = new PGobject();
            jsonObject.setType("json");

            if (value instanceof String string) {
                jsonObject.setValue(string);
            } else if (value instanceof Map || value instanceof List) {
                jsonObject.setValue(jsonb.toJson(value));
            } else {
                throw new MyDatabaseException("Invalid JSON content: value type is " + value.getClass().getName());
            }

            return jsonObject;

        } catch (SQLException e) {
            throw new MyDatabaseException("Error converting to PGobject for JSON type: " + e.getMessage());
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isTrue(Object value) {
        return Objects.equals(value, Boolean.TRUE) || "true".equalsIgnoreCase(value.toString());
    }
}
