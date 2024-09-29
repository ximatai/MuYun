package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.model.SortColumn;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.math.BigDecimal;
import java.util.HashMap;

public interface ISortAbility extends ISelectAbility, IUpdateAbility {

    default SortColumn getSortColumn() {
        return SortColumn.ORDER;
    }

    @GET
    @Path("update/{id}/sort")
    @Transactional
    @Operation(summary = "调整数据顺序")
    default Integer sort(@PathParam("id") String id,
                         @Parameter(description = "新位置之前数据的id") @QueryParam("prevId") String prevId,
                         @Parameter(description = "新位置之后数据的id") @QueryParam("nextId") String nextId,
                         @Parameter(description = "新位置的父节点id（仅Tree模式有效）") @QueryParam("parentId") String parentId
    ) {
        String sortColumn = getSortColumn().getColumnName();

        BigDecimal prev = BigDecimal.ZERO;
        BigDecimal next = BigDecimal.ZERO;

        if (prevId != null) {
            prev = (BigDecimal) this.view(prevId).get(sortColumn);
        }

        if (nextId != null) {
            next = (BigDecimal) this.view(nextId).get(sortColumn);
        }

        BigDecimal now = next.compareTo(prev) > 0 ? prev.add(next).divide(BigDecimal.valueOf(2)) : prev.add(BigDecimal.ONE);

        HashMap<String, Object> body = new HashMap<>();
        body.put(sortColumn, now);

        String condition = "";
        HashMap<String, Object> params = new HashMap<>();

        if (this instanceof ITreeAbility treeAbility) { // tree结构，要支持 parentId 的设置
            if (parentId == null || parentId.isEmpty()) {
                parentId = treeAbility.getParentKeyColumn().getDefaultValue().toString();
            }

            String parentKey = treeAbility.getParentKeyColumn().getName();
            body.put(parentKey, parentId);
            condition = " and " + parentKey + " = :pid ";
            params.put("pid", parentId);
        }

        int resCount = this.update(id, body);

        if (resCount == 1) { // 说明更新命中数据成功
            String sql = """
                with cte as (select %s, row_number() over (order by %s) as n_order
                             from %s where 1=1 %s
                )
                update %s set %s = (select n_order from cte where cte.%s = %s.%s)
                where %s.%s in (select %s from cte)
                """.formatted(getPK(), sortColumn,
                getMainTable(), condition,
                getMainTable(), sortColumn, getPK(), getMainTable(), getPK(),
                getMainTable(), getPK(), getPK()
            );

            getDB().update(sql, params);
            return resCount;
        }

        return 0;
    }
}
