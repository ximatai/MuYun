package net.ximatai.muyun.ability;

public interface IMetadataAbility {

    String getSchemaName();

    String getMainTable();

    default String getPK() {
        return "id";
    }

    default String getSelectOneRowSql() {
        return "select * from %s.%s where %s =:id".formatted(getSchemaName(), getMainTable(), getPK());
    }

    default String getSelectSql() {
        return "select * from %s.%s".formatted(getSchemaName(), getMainTable());
    }

}
