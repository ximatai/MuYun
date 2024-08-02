package net.ximatai.muyun.ability.std;

import java.util.Map;
import java.util.StringJoiner;

public interface IMetadataAbility {

    String getMainTable();

    default String getPK() {
        return "id";
    }

    default String getSelectOneRowSql() {
        return "select * from " + getMainTable() + " where " + getPK() + "=:id";
    }

    default String getInsertSql(Map<String, Object> params) {
        StringJoiner columns = new StringJoiner(", ", "(", ")");
        StringJoiner values = new StringJoiner(", ", "(", ")");
        params.keySet().forEach(key -> {
            columns.add(key);
            values.add(":" + key);
        });

        return "INSERT INTO " + getMainTable() + " " + columns + " VALUES " + values + " RETURNING " + getPK();
    }

    default String getUpdateSql(Map<String, Object> params) {
        StringJoiner setClause = new StringJoiner(", ");
        params.keySet().forEach(key -> {
            setClause.add(key + "=:" + key);
        });
        return "UPDATE " + getMainTable() + " SET " + setClause + " WHERE " + getPK() + "=:id";
    }

    default String getDeleteSql() {
        return "DELETE FROM " + getMainTable() + " WHERE " + getPK() + "=:id";
    }

}
