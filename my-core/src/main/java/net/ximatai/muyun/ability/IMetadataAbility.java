package net.ximatai.muyun.ability;

import net.ximatai.muyun.domain.OrderColumn;

import java.util.List;

public interface IMetadataAbility {

    String getSchemaName();

    String getMainTable();

    default String getPK() {
        return "id";
    }

    default OrderColumn getOrderColumn() {
        return OrderColumn.T_CREATE;
    }

    default List<OrderColumn> getOrderColumns() {
        return List.of(getOrderColumn());
    }

    default String getSelectOneRowSql() {
        return "select * from %s.%s where %s =:id".formatted(getSchemaName(), getMainTable(), getPK());
    }

    default String getSelectSql() {
        return "select * from %s.%s".formatted(getSchemaName(), getMainTable());
    }

}
