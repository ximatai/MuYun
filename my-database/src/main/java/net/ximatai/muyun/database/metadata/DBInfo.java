package net.ximatai.muyun.database.metadata;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
            .orElseThrow(() -> new IllegalArgumentException("Schema not found: " + schemaName));
    }

    public Map<String, DBTable> getTables() {
        Map<String, DBTable> tables = new HashMap<>();
        schemas.forEach(
            schema -> {
                tables.putAll(schema.getTables());
            }
        );
        return tables;
    }

    public boolean containsTable(String tableName) {
        return getTables().containsKey(tableName);
    }

    public String getName() {
        return name;
    }

}
