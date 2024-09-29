package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.builder.Column;

public interface ISoftDeleteAbility {

    default Column getSoftDeleteColumn() {
        return Column.DELETE_FLAG;
    }

}
