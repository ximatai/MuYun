package net.ximatai.muyun.ability;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.model.ChildTableInfo;

/**
 * 成为子表的能力
 */
public interface IChildAbility extends ICURDAbility, IMetadataAbility {

    default ChildTableInfo toChildTable(String foreignKey) {
        if (checkColumn(foreignKey)) {
            return new ChildTableInfo(foreignKey, this);
        } else {
            throw new RuntimeException("foreignKey not found");
        }
    }

    @Path("/placeholder")
    default String placeholder() {
        return "placeholder";
    }

}
