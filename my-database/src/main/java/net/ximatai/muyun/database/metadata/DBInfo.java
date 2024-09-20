package net.ximatai.muyun.database.metadata;

import java.util.HashSet;
import java.util.Set;

public class DBInfo {
    private String name;

    private Set<DBSchema> schemas = new HashSet<>();

    public DBInfo(String name) {
        this.name = name;
    }

    public DBInfo addSchema(DBSchema schema) {
        this.schemas.add(schema);
        return this;
    }

    public DBSchema getSchema(String schemaName) {
        return schemas.stream()
            .filter(schema -> schemaName.equals(schema.getName()))
            .findFirst()
            .orElse(null);
    }

    public String getName() {
        return name;
    }

}
