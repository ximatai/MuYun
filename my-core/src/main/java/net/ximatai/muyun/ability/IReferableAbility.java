package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.model.ReferenceInfo;

import java.util.List;

public interface IReferableAbility extends IMetadataAbility, ILabelAbility {

    default String getKeyColumn() {
        return getPK();
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

    default ReferenceInfo toReferenceInfo(String foreignKey) {
        if (checkColumnExist(foreignKey)) {
            return new ReferenceInfo(foreignKey, this);
        } else {
            throw new RuntimeException("foreignKey not found");
        }
    }

}
