package net.ximatai.muyun.database.metadata;

import net.ximatai.muyun.database.builder.TableBase;
import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class DBTable extends TableBase {
    private Jdbi jdbi;

    private Map<String, DBColumn> columnMap;

    public DBTable(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public DBTable setName(String name) {
        this.name = name;
        return this;
    }

    public DBTable setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public Map<String, DBColumn> getColumnMap() {
        if (columnMap == null) {
            jdbi.useHandle(handle -> {
                Connection connection = handle.getConnection();
                try {
                    DatabaseMetaData metaData = connection.getMetaData();
                    Map<String, DBColumn> columnMap = new HashMap<>();
                    try (ResultSet rs = metaData.getColumns(null, schema, name, null)) {
                        while (rs.next()) {
                            DBColumn column = new DBColumn();
                            column.setName(
                                rs.getString("COLUMN_NAME")
                            );
                            column.setType(rs.getString("TYPE_NAME"));
                            column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                            var defaultValue = rs.getString("COLUMN_DEF");
                            column.setDefaultValue(defaultValue);
                            if ("YES".equals(rs.getString("IS_AUTOINCREMENT"))) {
                                column.setSequence();
                            }
                            if (defaultValue != null && defaultValue.startsWith("nextval(")) {
                                column.setSequence();
                            }
                            column.setDescription(rs.getString("REMARKS"));

                            columnMap.put(column.getName(), column);
                        }
                    }

                    // Primary keys
                    try (ResultSet rs = metaData.getPrimaryKeys(null, schema, name)) {
                        while (rs.next()) {
                            String primaryKeyColumn = rs.getString("COLUMN_NAME");
                            DBColumn column = columnMap.get(primaryKeyColumn);
                            if (column != null) {
                                column.setPrimaryKey(true);
                            }
                        }
                    }

                    // index
                    try (ResultSet rs = metaData.getIndexInfo(null, schema, name, false, false)) {
                        while (rs.next()) {
                            String columnName = rs.getString("COLUMN_NAME");
                            DBColumn column = columnMap.get(columnName);
                            if (column != null) {
                                column.setIndexName(rs.getString("INDEX_NAME"));
                                column.setIndexed(true);
                                if (!rs.getBoolean("NON_UNIQUE")) {
                                    column.setUnique(true);
                                }
                            }
                        }
                    }

                    this.columnMap = columnMap;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return columnMap;
    }

    public boolean contains(String column) {
        if (column == null) {
            return false;
        }
        return getColumn(column.toLowerCase()) != null;
    }

    public DBColumn getColumn(String column) {
        Objects.requireNonNull(column);
        return getColumnMap().get(column.toLowerCase());
    }

    public void resetColumns() {
        this.columnMap = null;
    }
}
