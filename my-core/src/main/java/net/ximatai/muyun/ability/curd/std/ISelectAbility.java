package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.ability.IDesensitizationAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ISecurityAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.core.exception.QueryException;
import net.ximatai.muyun.database.builder.TableBase;
import net.ximatai.muyun.database.tool.DateTool;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.SortColumn;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ISelectAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default SortColumn getDefatultSortColumn() {
        if (this instanceof ISortAbility ability) {
            return ability.getSortColumn();
        }

        if (checkColumn(SortColumn.SORT.getColumnName())) {
            return SortColumn.SORT;
        }
        if (checkColumn(SortColumn.CREATE.getColumnName())) {
            return SortColumn.CREATE;
        }
        return null;
    }

    default List<SortColumn> getSortDefaultColumns() {
        SortColumn orderColumn = getDefatultSortColumn();
        if (orderColumn == null) {
            return List.of();
        } else {
            return List.of(getDefatultSortColumn());
        }
    }

    default String getSelectOneRowSql() {
        return "select * from (%s) %s where %s = :id ".formatted(getSelectSql(), getMainTable(), getPK());
    }

    default String getSelectSql() {
        if (this instanceof ICustomSelectSqlAbility ability) {
            return ability.getCustomSql();
        }

        StringBuilder starSql = new StringBuilder("%s.*".formatted(getMainTable()));
        StringBuilder joinSql = new StringBuilder();
        String softDeleteSql = "";
        if (this instanceof ISoftDeleteAbility ability) {
            softDeleteSql = " and %s.%s = false ".formatted(getMainTable(), ability.getSoftDeleteColumn().getName());
        }

        if (this instanceof IReferenceAbility referenceAbility) {

            referenceAbility.getReferenceList().forEach(info -> {
                TableBase referenceTable = info.getReferenceTable();
                String referenceTableTempName = "%s_%s".formatted(referenceTable.getName(), UUID.randomUUID().toString().substring(25));
                info.getTranslates().forEach((column, alias) -> {
                    starSql.append(",%s.%s as %s ".formatted(referenceTableTempName, column, alias));
                });

                joinSql.append("\n left join %s.%s as %s on %s.%s = %s.%s ".formatted(referenceTable.getSchema(), referenceTable.getName(), referenceTableTempName, getMainTable(), info.getRelationColumn(), referenceTableTempName, info.getHitField()));
            });

        }

        return "select %s from %s.%s %s where 1=1 %s ".formatted(starSql, getSchemaName(), getMainTable(), joinSql, softDeleteSql);
    }

    @GET
    @Path("/view/{id}")
    @Operation(summary = "查看指定的数据")
    default Map<String, ?> view(@PathParam("id") String id) {
        Map<String, Object> row = getDB().row(getSelectOneRowSql(), Map.of("id", id));
        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.decrypt(row);
            securityAbility.checkSign(row);
        }
        if (this instanceof IDesensitizationAbility desensitizationAbility) {
            desensitizationAbility.desensitize(row);
        }
        return row;
    }

    @GET
    @Path("/view")
    @Operation(summary = "分页查询")
    default PageResult view(@Parameter(description = "页码") @QueryParam("page") Integer page,
                            @Parameter(description = "分页大小") @QueryParam("size") Long size,
                            @Parameter(description = "是否分页") @QueryParam("noPage") Boolean noPage,
                            @Parameter(description = "排序", example = "t_create,desc") @QueryParam("sort") List<String> sort) {
        return view(page, size, noPage, sort, null, null);
    }

    default PageResult view(Integer page,
                            Long size,
                            Boolean noPage,
                            List<String> sort,
                            Map<String, Object> queryBody,
                            List<QueryItem> queryItemList
    ) {
        List<Object> params = new ArrayList<>();

        List<SortColumn> orderColumns = new ArrayList<>();

        if (sort != null && !sort.isEmpty()) {
            sort.forEach(s -> {
                String[] strings = s.split(",");
                String order = "ASC";
                if (strings.length > 1) {
                    order = strings[1];
                }
                orderColumns.add(new SortColumn(strings[0], order));
            });
        }

        if (orderColumns.isEmpty()) {
            orderColumns.addAll(getSortDefaultColumns());
        }

        String authCondition = "and 1=1";
        StringBuilder queryCondition = new StringBuilder();

        // 查询条件处理
        if (queryBody != null && queryItemList != null && !queryItemList.isEmpty()) {
            queryBody.forEach((k, v) -> {
                StringBuilder condition = new StringBuilder();
                QueryItem qi = queryItemList.stream().filter(item -> item.getAlias().equals(k)).findFirst().orElse(null);

                if (qi == null) {
                    throw new QueryException("查询条件%s未配置，查询失败".formatted(k));
                }

                condition.append(" and %s ".formatted(qi.getColumn()));

                if (v == null) {
                    condition.append(" isnull ");
                    queryCondition.append(condition);
                    return;
                }

                QueryItem.SymbolType symbolType = qi.getSymbolType();

                if (qi.isDate() || qi.isDatetime()) { // 是日期，需要提前转换
                    Function<String, Date> converter = s -> qi.isDate() ? DateTool.stringToSqlDate(s) : DateTool.stringToSqlTimestamp(s);

                    if (v instanceof String s) {
                        v = converter.apply(s);
                    } else if (v instanceof List<?> list) {
                        v = list.stream().map(o -> o instanceof String s ? converter.apply(s) : o).toList();
                    }
                }

                switch (symbolType) {
                    case LIKE:
                        condition.append(" like ? ");
                        params.add("%" + v + "%");
                        break;
                    case IN, NOT_IN:
                        if (!(v instanceof List list)) {
                            throw new QueryException("IN 条件的值必须是列表");
                        }

                        if (list.isEmpty()) {
                            list.add("muyuntage_20240903_nanjing");
                        }

                        String symbol = qi.getSymbolType().equals(QueryItem.SymbolType.IN) ? "in" : "not in";
                        condition.append(" %s (%s) ".formatted(symbol, list.stream().map(x -> "?").collect(Collectors.joining(","))));
                        params.addAll(list);
                        break;
                    case EQUAL, NOT_EQUAL:
                        String notMark = symbolType.equals(QueryItem.SymbolType.NOT_EQUAL) ? "!" : "";
                        condition.append(" %s= ? ".formatted(notMark));
                        params.add(v);
                        break;
                    case RANGE:
                        if (!(v instanceof List list) || list.size() != 2) {
                            throw new QueryException("区间查询%s的内容必须是长度为2的数组".formatted(k));
                        }

                        Object a = list.get(0);
                        Object b = list.get(1);

                        if (a == null) {
                            condition.append(" = %s ".formatted(qi.getColumn()));
                        } else {
                            condition.append(" >= ? ");
                            params.add(a);
                        }

                        if (b != null) {
                            condition.append(" and %s ".formatted(qi.getColumn()));
                            condition.append(" <= ? ");
                            params.add(b);
                        }
                        break;
                    default:
                        throw new QueryException("不支持的符号类型: " + symbolType);
                }

                queryCondition.append(condition);
            });
        }

        String baseSql = "select * from (%s) %s where 1=1 %s %s ".formatted(getSelectSql(), getMainTable(), authCondition, queryCondition);

        // 计算总数
        long total = (long) getDB().row("select count(*) as num from (%s) %s where 1=1 %s %s ".formatted(getSelectSql(), getMainTable(), authCondition, queryCondition), params).get("num");

        // 构建查询 SQL
        StringBuilder querySql = new StringBuilder(baseSql);

        // 添加排序列
        if (!orderColumns.isEmpty()) {
            querySql.append(" order by ");
            querySql.append(orderColumns.stream()
                .filter(oc -> checkColumn(oc.getColumnName()))
                .map(oc -> "%s %s".formatted(oc.getColumnName(), oc.getType().isASC() ? " asc" : " desc"))
                .collect(Collectors.joining(","))
            );
        }

        page = page == null ? 1 : page;
        size = size == null ? 10 : size;

        if (noPage != null && noPage) {
            size = total;
            page = 0;
        } else { // 添加分页参数
            querySql.append(" offset ? limit ? ");
            params.add((page - 1) * size);
            params.add(size);
        }

        List<Map<String, Object>> list = getDB().query(querySql.toString(), params);

        if (this instanceof ISecurityAbility securityAbility) {
            list.forEach(securityAbility::decrypt);
            list.forEach(securityAbility::checkSign);
        }

        if (this instanceof IDesensitizationAbility desensitizationAbility) {
            list.forEach(desensitizationAbility::desensitize);
        }

        return new PageResult<>(list, total, size, page);
    }

}
