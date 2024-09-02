package net.ximatai.muyun.ability;

public interface IMetadataAbility {

    String getMainTable();

    default String getPK() {
        return "id";
    }

    default String getSelectOneRowSql() {
        return "select * from " + getMainTable() + " where " + getPK() + "=:id";
    }

}
