package net.ximatai.muyun.ability;

import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.Column;

/**
 * 软删除的能力
 */
public interface ISoftDeleteAbility {

    default Column getSoftDeleteColumn() {
        return PresetColumn.DELETE_FLAG;
    }

}
