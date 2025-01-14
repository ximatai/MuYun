package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.*;
import net.ximatai.muyun.core.exception.QueryException;
import net.ximatai.muyun.database.builder.TableBase;
import net.ximatai.muyun.database.tool.DateTool;
import net.ximatai.muyun.model.ApiRequest;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryGroup;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.SortColumn;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 查询数据的能力（单条+多条分页）
 */
public interface ISelectAbility extends IDatabaseAbilityStd, IMetadataAbility {

    default SortColumn getDefatultSortColumn() {
        if (this instanceof ISortAbility ability) {
            return ability.getSortColumn();
        }

        if (this instanceof ICodeGenerateAbility ability
            && ability.getCodeColumn().equals(SortColumn.CODE.getColumnName())) {
            return SortColumn.CODE;
        }

        if (checkColumn(SortColumn.ORDER.getColumnName())) {
            return SortColumn.ORDER;
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
        return "select * from (%s) %s where 1=1 %s and %s = :id ".formatted(getSelectSql(), getMainTable(), getAuthCondition(), getPK());
    }

    default String getSelectSql() {
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
                StringBuilder other = new StringBuilder();
                info.getOtherConditions().forEach((column, value) -> {
                    other.append(" and %s.%s = %s ".formatted(referenceTableTempName, column, value));
                });

                if (info.isDeep()) {
                    joinSql.append("\n left join (%s) as %s on %s.%s = %s.%s %s "
                        .formatted(info.getDeepSelectSql(),
                            referenceTableTempName, getMainTable(), info.getRelationColumn(),
                            referenceTableTempName, info.getHitField(), other));
                } else {
                    joinSql.append("\n left join %s as %s on %s.%s = %s.%s %s "
                        .formatted(referenceTable.getSchemaDotTable(), referenceTableTempName,
                            getMainTable(), info.getRelationColumn(),
                            referenceTableTempName, info.getHitField(), other));
                }
            });

        }

        String mainTable = getSchemaDotTable();
        if (this instanceof ICustomSelectSqlAbility ability) {
            mainTable = "(%s) as %s ".formatted(ability.getCustomSql(), getMainTable());
        }

        return "select %s from %s %s where 1=1 %s ".formatted(starSql, mainTable, joinSql, softDeleteSql);
    }

    default void processEachRow(Map row) {
        if (this instanceof ISecurityAbility securityAbility) {
            securityAbility.decrypt(row);
            securityAbility.checkSign(row);
        }
        if (this instanceof IDesensitizationAbility desensitizationAbility) {
            desensitizationAbility.desensitize(row);
        }
    }

    @GET
    @Path("/view/{id}")
    @Operation(summary = "查看指定的数据")
    default Map<String, Object> view(@PathParam("id") String id) {
        Map<String, Object> row = getDB().row(getSelectOneRowSql(), Map.of("id", id));

        if (row == null) {
            return null;
        }
        processEachRow(row);
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

    /**
     * @return 权限条件 SQL
     */
    default String getAuthCondition() {
        String authCondition = "";
        if (this instanceof IAuthAbility authAbility) {
            ApiRequest apiRequest = authAbility.getApiRequest();

            if (apiRequest.getAuthCondition() != null) {
                authCondition = apiRequest.getAuthCondition();
                apiRequest.setAuthCondition(null); // 权限条件不会被多次使用
            }
        }
        return authCondition;
    }

    default PageResult view(Integer page,
                            Long size,
                            Boolean noPage,
                            List<String> sort,
                            Map<String, Object> queryBody,
                            QueryGroup queryGroup
    ) {
        return this.view(page, size, noPage, sort, queryBody, queryGroup, null);
    }

    default PageResult view(Integer page,
                            Long size,
                            Boolean noPage,
                            List<String> sort,
                            Map<String, Object> queryBody,
                            QueryGroup queryGroup,
                            String authCondition
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
                if (checkColumn(strings[0])) {
                    orderColumns.add(new SortColumn(strings[0], order));
                }
            });
        }

        if (orderColumns.isEmpty()) {
            orderColumns.addAll(getSortDefaultColumns());
        }

        if (authCondition == null) {
            authCondition = getAuthCondition();
        }

        StringBuilder queryCondition = new StringBuilder();

        // 查询条件处理
        if (queryBody != null && queryGroup != null) {
            buildUpQuery(queryCondition, queryGroup, queryBody, params, " and ");
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

        list.forEach(this::processEachRow);

        return new PageResult<>(list, total, size, page);
    }

    private void buildUpQuery(StringBuilder condition, QueryGroup group, Map<String, Object> queryBody, List<Object> params, String sqlSymbol) {
        QueryItem qi = group.getQueryItem();

        StringBuilder part = new StringBuilder();

        if (qi != null && (queryBody.containsKey(qi.getAlias()) || qi.getDefaultValue() != null)) {
            Object v = queryBody.get(qi.getAlias());

            if (!queryBody.containsKey(qi.getAlias())) { // queryItem 的默认值参与查询
                v = qi.getDefaultValue();
            }

            if (!(v instanceof String str) || !str.isBlank()) { // 字符串为空不参与查询
                buildUpCondition(part, qi, v, params);
            }
        }

        if (part.isEmpty()) {
            part.append("1=1");
        }

        if (!group.getAndGroups().isEmpty()) {
            for (QueryGroup queryGroup : group.getAndGroups()) {
                buildUpQuery(part, queryGroup, queryBody, params, " and ");
            }
        }

        if (!group.getOrGroups().isEmpty()) {
            for (QueryGroup queryGroup : group.getOrGroups()) {
                buildUpQuery(part, queryGroup, queryBody, params, " or ");
            }
        }

        if (part.isEmpty()) {
            part.append("1=1");
        }

        condition.append(sqlSymbol);
        condition.append("(");
        condition.append(part);
        condition.append(")");

    }

    private void buildUpCondition(StringBuilder part, QueryItem qi, Object v, List<Object> params) {
        part.append(" %s ".formatted(qi.getColumn()));

        if (v == null) {
            part.append(" isnull ");
        } else {
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
                    part.append(" like ? ");
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
                    part.append(" %s (%s) ".formatted(symbol, list.stream().map(x -> "?").collect(Collectors.joining(","))));
                    params.addAll(list);
                    break;
                case EQUAL, NOT_EQUAL:
                    String notMark = symbolType.equals(QueryItem.SymbolType.NOT_EQUAL) ? "!" : "";
                    part.append(" %s= ? ".formatted(notMark));
                    params.add(v);
                    break;
                case PG_ARRAY_EQUAL, PG_ARRAY_OVERLAP, PG_ARRAY_CONTAIN, PG_ARRAY_BE_CONTAIN:
                    if (!(v instanceof List list)) {
                        throw new QueryException("数据比较时参数也应为数组");
                    }

                    String s = "=";
                    if (symbolType.equals(QueryItem.SymbolType.PG_ARRAY_OVERLAP)) {
                        s = "&&";
                    }
                    if (symbolType.equals(QueryItem.SymbolType.PG_ARRAY_CONTAIN)) {
                        s = "<@";
                    }
                    if (symbolType.equals(QueryItem.SymbolType.PG_ARRAY_BE_CONTAIN)) {
                        s = "@>";
                    }

                    String type = "varchar";

                    if (!list.isEmpty() && list.get(0) instanceof Integer) {
                        type = "int";
                    }

                    part.append(" %s ? ".formatted(s));
                    params.add(getDB().createArray(list, type));
                    break;
                case RANGE:
                    if (!(v instanceof List list) || list.size() != 2) {
                        throw new QueryException("区间查询%s的内容必须是长度为2的数组".formatted(qi.getAlias()));
                    }

                    Object a = list.get(0);
                    Object b = list.get(1);

                    if (a == null) {
                        part.append(" = %s ".formatted(qi.getColumn()));
                    } else {
                        part.append(" >= ? ");
                        params.add(a);
                    }

                    if (b != null) {
                        part.append(" and %s ".formatted(qi.getColumn()));
                        part.append(" <= ? ");

                        // b 是时间，但是b跟a的时间相同，并且 a 时间的 时分秒都是0，就把b的时分秒补成 23:59:59.999
                        if (b instanceof Timestamp bTime && b.equals(a)
                            && bTime.toLocalDateTime().getHour() == 0
                            && bTime.toLocalDateTime().getMinute() == 0
                            && bTime.toLocalDateTime().getSecond() == 0) {

                            Calendar cal = Calendar.getInstance();
                            cal.setTime(bTime);
                            cal.set(Calendar.HOUR_OF_DAY, 23);
                            cal.set(Calendar.MINUTE, 59);
                            cal.set(Calendar.SECOND, 59);
                            cal.set(Calendar.MILLISECOND, 999);
                            b = new Timestamp(cal.getTimeInMillis());
                        }

                        params.add(b);
                    }
                    break;
                default:
                    throw new QueryException("不支持的符号类型: " + symbolType);
            }
        }
    }

}
