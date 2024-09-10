package net.ximatai.muyun.ability;

import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.model.ChildTableInfo;

public interface IChildAbility extends ICURDAbility {

    default ChildTableInfo toChildTable(String foreignKey) {
        if (getDBTable().contains(foreignKey)) {
            return new ChildTableInfo(this, foreignKey);
        } else {
            throw new RuntimeException("foreignKey not found");
        }
    }

    default String getChildAlias() {
        return getMainTable();
    }

}
