package net.ximatai.muyun.ability;

import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.model.SortColumn;

public interface ISortAbility extends IUpdateAbility {

    default SortColumn getSortColumn() {
        return SortColumn.SORT;
    }

}
