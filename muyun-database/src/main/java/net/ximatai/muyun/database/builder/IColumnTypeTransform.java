package net.ximatai.muyun.database.builder;

public interface IColumnTypeTransform {

    IColumnTypeTransform POSTGRES = type -> switch (type) {
        case VARCHAR_ARRAY -> "varchar[]";
        case JSON -> "jsonb";
        default -> type.name();
    };

    IColumnTypeTransform MYSQL = type -> switch (type) {
        case VARCHAR_ARRAY -> "varchar";
        case JSON -> "json";
        default -> type.name();
    };

    String transform(DataType type);

}
