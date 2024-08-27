package net.ximatai.muyun.database.metadata;

import org.jdbi.v3.core.Jdbi;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DBTable {
    private Jdbi jdbi;

    private String name;
    private String schema;
    private List<DBColumn> columns;

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

    public DBTable setColumns(List<DBColumn> columns) {
        this.columns = columns;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public List<DBColumn> getColumns() {
        if (columns == null) {
            jdbi.useHandle(handle -> {
                Connection connection = handle.getConnection();
                try {
                    DatabaseMetaData metaData = connection.getMetaData();
                    List<DBColumn> columns = new ArrayList<>();
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

                            columns.add(column);
                        }
                    }

                    // Primary keys
                    try (ResultSet rs = metaData.getPrimaryKeys(null, schema, name)) {
                        while (rs.next()) {
                            String primaryKeyColumn = rs.getString("COLUMN_NAME");
                            for (DBColumn column : columns) {
                                if (column.getName().equals(primaryKeyColumn)) {
                                    column.setPrimaryKey(true);
                                    break;
                                }
                            }
                        }
                    }

                    // Uniqueness (including primary key)
                    try (ResultSet rs = metaData.getIndexInfo(null, schema, name, true, false)) {
                        while (rs.next()) {
                            String uniqueColumn = rs.getString("COLUMN_NAME");
                            for (DBColumn column : columns) {
                                if (column.getName().equals(uniqueColumn)) {
                                    column.setUnique(true);
                                    break;
                                }
                            }
                        }
                    }

                    this.columns = columns;
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });
        }
        return columns;
    }

    public boolean contains(String column) {
        return getColumn(column) != null;
    }

    public DBColumn getColumn(String column) {
        return getColumns().stream().filter(c -> c.getName().equals(column)).findFirst().orElse(null);
    }

    public void resetColumns() {
        this.columns = null;
    }
}
