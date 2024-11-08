package net.ximatai.muyun.database.builder;

public enum DataType {
    VARCHAR,
    INT,
    BOOLEAN,
    TIMESTAMP,
    DATE,
    NUMERIC,
    JSON,
    VARCHAR_ARRAY;

    String getType(IColumnTypeTransform transform) {
        return transform.transform(this);
    }

}
