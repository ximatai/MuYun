package net.ximatai.muyun.database.builder;

public enum DataType {
    VARCHAR("varchar"),
    INT("int"),
    BOOLEAN("boolean"),
    TIMESTAMP("timestamp"),
    DATE("date"),
    NUMERIC("numeric"),
    JSON("jsonb"),
    VARCHAR_ARRAY("varchar[]");

    private final String type;

    DataType(String type) {
        this.type = type;
    }
}
