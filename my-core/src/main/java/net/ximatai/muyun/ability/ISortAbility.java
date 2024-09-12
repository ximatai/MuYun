package net.ximatai.muyun.ability;

import net.ximatai.muyun.model.SortColumn;

public interface ISortAbility {

    default SortColumn getSortColumn() {
        return SortColumn.SORT;
    }

}
