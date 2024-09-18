package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IMetadataAbility;

public class DataChangeChannel {

    private final String schema;
    private final String table;

    public DataChangeChannel(String schema, String table) {
        this.schema = schema;
        this.table = table;
    }

    public DataChangeChannel(IMetadataAbility metadataAbility) {
        this.schema = metadataAbility.getSchemaName();
        this.table = metadataAbility.getMainTable();
    }

    public String getAddress() {
        return "data.change.%s.%s".formatted(schema, table).toLowerCase();
    }

    public String getAddressWithType(Type type) {
        return "data.%s.%s.%s".formatted(type, schema, table).toLowerCase();
    }

    public enum Type {
        CREATE, UPDATE, DELETE
    }

}
