package net.ximatai.muyun.database.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TableWrapper extends TableBase {

    private String comment;

    private List<TableBase> inherits = new ArrayList<>();
    private List<Column> columns = new ArrayList<>();
    private List<Index> indexes = new ArrayList<>();

    private Column primaryKey;

    public static TableWrapper withName(String name) {
        return new TableWrapper(name);
    }

    public TableWrapper setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public TableWrapper setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public TableWrapper setInherits(List<TableBase> inherits) {
        this.inherits = inherits;
        return this;
    }

    public TableWrapper setInherit(TableBase inherit) {
        this.inherits = List.of(inherit);
        return this;
    }

    public TableWrapper setPrimaryKey(String name) {
        primaryKey = Column.of(name).setPrimaryKey().setType(DataType.VARCHAR).setNullable(false);
        return this;
    }

    public TableWrapper setPrimaryKey(Column column) {
        primaryKey = column;
        return this;
    }

    public TableWrapper addIndex(String columnName) {
        this.addIndex(columnName, false);
        return this;
    }

    public TableWrapper addIndex(String columnName, boolean unique) {
        Column col = getColumns().stream().filter(column -> column.getName().equals(columnName)).findFirst().orElse(null);
        if (col == null) {
            throw new IllegalArgumentException("No such column: " + columnName);
        }

        indexes.add(new Index(columnName, unique));
        return this;
    }

    public TableWrapper addIndex(List<String> columns) {
        columns.forEach(columnName -> {
            Column col = getColumns().stream().filter(column -> column.getName().equals(columnName)).findFirst().orElse(null);
            if (col == null) {
                throw new IllegalArgumentException("No such column: " + columnName);
            }
        });

        this.addIndex(columns, false);
        return this;
    }

    public TableWrapper addIndex(List<String> columns, boolean unique) {
        indexes.add(new Index(columns, unique));
        return this;
    }

    public TableWrapper addColumn(String columnName) {
        columns.add(Column.of(columnName));
        return this;
    }

    public TableWrapper addColumn(String columnName, String comment) {
        columns.add(Column.of(columnName).setComment(comment));
        return this;
    }

    public TableWrapper addColumn(String columnName, String comment, Object defaultValue) {
        columns.add(Column.of(columnName).setComment(comment).setDefaultValue(defaultValue));
        return this;
    }

    public TableWrapper addColumn(Column column) {
        if (primaryKey != null && Objects.equals(column.getName(), primaryKey.getName())) {
            throw new IllegalArgumentException("Primary key already exists");
        }

        columns.add(column);

        if (column.isUnique()) {
            addIndex(column.getName(), true);
        } else if (column.isIndexed()) {
            addIndex(column.getName());
        }

        return this;
    }

    public String getComment() {
        return comment;
    }

    public List<TableBase> getInherits() {
        return inherits;
    }

    public List<Column> getColumns() {
        return columns;
    }

    public List<Index> getIndexes() {
        return indexes;
    }

    public Column getPrimaryKey() {
        return primaryKey;
    }

    public TableWrapper(String name) {
        this.name = name;
    }

}
