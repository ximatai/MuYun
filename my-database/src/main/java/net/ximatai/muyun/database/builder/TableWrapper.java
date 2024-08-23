package net.ximatai.muyun.database.builder;

import java.util.ArrayList;
import java.util.List;

public class TableWrapper {

    private String name;
    private String schema;
    private String comment;

    private List<String> inherits = new ArrayList<>();
    private List<Column> columns = new ArrayList<>();
    private List<Index> indexes = new ArrayList<>();

    private Column primaryKey;

    TableWrapper setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    TableWrapper setComment(String comment) {
        this.comment = comment;
        return this;
    }

    TableWrapper setInherits(List<String> inherits) {
        this.inherits = inherits;
        return this;
    }

    TableWrapper setPrimaryKey(String name) {
        primaryKey = new Column(name).setPrimaryKey();
        return this;
    }

    TableWrapper addIndex(String columnName, boolean unique) {
        indexes.add(new Index(columnName, unique));
        return this;
    }

    TableWrapper addIndex(List<String> columns, boolean unique) {
        indexes.add(new Index(columns, unique));
        return this;
    }

    TableWrapper addColumn(String columnName) {
        columns.add(new Column(columnName));
        return this;
    }

    TableWrapper addColumn(Column column) {
        columns.add(column);
        return this;
    }


    private static class Index {
        List<String> columns;
        boolean unique;

        public Index(String columnName, boolean unique) {
            this.columns = new ArrayList<>();
            this.columns.add(columnName);
            this.unique = unique;
        }

        public Index(List<String> columns, boolean unique) {
            this.columns = columns;
            this.unique = unique;
        }
    }

    public String getName() {
        return name;
    }

    public String getSchema() {
        return schema;
    }

    public String getComment() {
        return comment;
    }

    public List<String> getInherits() {
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
