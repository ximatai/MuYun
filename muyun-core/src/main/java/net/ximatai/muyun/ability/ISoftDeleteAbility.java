package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.builder.Column;

/**
 * 软删除的能力
 */
public interface ISoftDeleteAbility {

    default Column getSoftDeleteColumn() {
        return Column.DELETE_FLAG;
    }

}
