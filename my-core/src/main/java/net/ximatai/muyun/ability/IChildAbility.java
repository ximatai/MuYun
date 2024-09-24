package net.ximatai.muyun.ability;

import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.model.ChildTableInfo;

public interface IChildAbility extends ICURDAbility, IMetadataAbility {

    default ChildTableInfo toChildTable(String foreignKey) {
        if (checkColumn(foreignKey)) {
            return new ChildTableInfo(foreignKey, this);
        } else {
            throw new RuntimeException("foreignKey not found");
        }
    }

    default String getChildAlias() {
        return getMainTable();
    }

}
