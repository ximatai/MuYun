package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.database.metadata.DBTable;
import net.ximatai.muyun.domain.OrderColumn;
import net.ximatai.muyun.domain.PageResult;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface ISelectAbility extends IDatabaseAbility, IMetadataAbility {

    @GET
    @Path("/view/{id}")
    default Map<String, ?> view(@PathParam("id") String id) {
        return getDatabase().row(getSelectOneRowSql(), Map.of("id", id));
    }

    @GET
    @Path("/view")
    default PageResult view(@QueryParam("page") int page, @QueryParam("limit") int limit, @QueryParam("orderField") String orderField, @QueryParam("orderType") String orderType) {
        DBTable dbTable = getDatabase().getDBInfo().getSchema(getSchemaName()).getTables().get(getMainTable());

        List<OrderColumn> orderColumns = (orderField != null) ? List.of(new OrderColumn(orderField, orderType)) : getOrderColumns();

        String authCondition = "and 1=1";
        String baseSql = "select * from (%s) %s where 1=1 %s".formatted(getSelectSql(), getMainTable(), authCondition);

        // 计算总数
        long total = (long) getDatabase().row("select count(*) as num from (%s) %s where 1=1 %s ".formatted(getSelectSql(), getMainTable(), authCondition)).get("num");

        // 构建查询 SQL
        StringBuilder querySql = new StringBuilder(baseSql);
        List<Object> params = new ArrayList<>();

        // 添加排序列
        if (!orderColumns.isEmpty()) {
            querySql.append(" order by ");
            querySql.append(orderColumns.stream()
                .filter(oc -> dbTable.contains(oc.getColumnName()))
                .map(oc -> "%s %s".formatted(oc.getColumnName(), oc.getType().isASC() ? " asc" : " desc"))
                .collect(Collectors.joining(","))
            );
        }

        // 添加分页参数
        querySql.append(" offset ? limit ? ");
        params.add((page - 1) * limit);
        params.add(limit);

        List<?> list = getDatabase().query(querySql.toString(), params);

        return new PageResult<>(list, total, limit, page);
    }
}
