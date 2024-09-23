package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.exception.MyDatabaseException;

import java.util.List;

public interface IReferableAbility extends IMetadataAbility {

    default String getKeyColumn() {
        return getPK();
    }

    default String getLabelColumn() {
        return "v_name";
    }

    default List<String> getOpenColumns() {
        return List.of();
    }

    default boolean checkColumnExist(String column) {
        if (!checkColumn(column)) {
            throw new MyDatabaseException("根据引用关系，要求 %s 必须含有 %s 字段".formatted(getMainTable(), column));
        }
        return true;
    }

}
