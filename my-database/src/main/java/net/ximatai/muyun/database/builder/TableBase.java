package net.ximatai.muyun.database.builder;

public class TableBase {
    protected String schema;
    protected String name;

    public TableBase setSchema(String schema) {
        this.schema = schema;
        return this;
    }

    public TableBase setName(String name) {
        this.name = name;
        return this;
    }

    public String getSchema() {
        return schema;
    }

    public String getName() {
        return name;
    }
}
