package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.model.SortColumn;

import java.math.BigDecimal;
import java.util.HashMap;

public interface ISortAbility extends ISelectAbility, IUpdateAbility {

    default SortColumn getSortColumn() {
        return SortColumn.SORT;
    }

    @GET
    @Path("update/{id}/sort")
    @Transactional
    default void sort(@PathParam("id") String id,
                      @QueryParam("prevId") String prevId,
                      @QueryParam("nextId") String nextId,
                      @QueryParam("parentId") String parentId
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

        if (parentId != null && this instanceof ITreeAbility treeAbility) { // tree结构，要支持 parentId 的设置
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

            getDatabase().update(sql, params);
        }

    }
}
