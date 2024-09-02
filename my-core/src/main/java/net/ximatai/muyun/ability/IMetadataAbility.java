package net.ximatai.muyun.ability;

import net.ximatai.muyun.domain.OrderColumn;

import java.util.List;

public interface IMetadataAbility {

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
        return "select * from " + getMainTable() + " where " + getPK() + "=:id";
    }

    default String getSelectSql() {
        return "select * from " + getMainTable();
    }

}
