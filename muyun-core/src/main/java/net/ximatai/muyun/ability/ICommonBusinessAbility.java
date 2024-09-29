package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.builder.Column;

import java.util.ArrayList;
import java.util.List;

public interface ICommonBusinessAbility {

    List<Column> BASE_COLUMNS = List.of(
        Column.of("t_create"),
        Column.of("t_update")
    );

    default List<Column> getCommonColumns() {
        if (this instanceof ISoftDeleteAbility) {
            List<Column> columns = new ArrayList<>(BASE_COLUMNS);
            columns.add(Column.of("t_delete"));
            return columns;
        }
        return BASE_COLUMNS;
    }

}
