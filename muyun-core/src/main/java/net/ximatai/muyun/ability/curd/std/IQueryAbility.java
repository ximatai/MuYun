package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;
import org.eclipse.microprofile.openapi.annotations.parameters.RequestBody;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 根据条件对数据进行复杂查询的能力
 */
public interface IQueryAbility extends ISelectAbility {

    List<QueryItem> queryItemList();

    @GET
    @Path("/queryColumns")
    @Operation(summary = "已配置可供查询的字段")
    default List<QueryItem> queryColumns() {
        return queryItemList().stream().filter(item -> !item.isHide()).collect(Collectors.toList());
    }

    @POST
    @Path("/view")
    @Operation(summary = "分页查询（带查询条件）")
    default PageResult view(@Parameter(description = "页码") @QueryParam("page") Integer page,
                            @Parameter(description = "分页大小") @QueryParam("size") Long size,
                            @Parameter(description = "是否分页") @QueryParam("noPage") Boolean noPage,
                            @Parameter(description = "排序", example = "t_create,desc") @QueryParam("sort") List<String> sort,
                            @RequestBody(description = "查询条件信息") Map<String, Object> queryBody) {
        return this.view(page, size, noPage, sort, queryBody, queryItemList());
    }

    default PageResult query(Map<String, Object> queryBody) {
        return this.view(null, null, true, null, queryBody, queryItemList());
    }

    default PageResult query(Map<String, Object> queryBody, String authCondition) {
        return this.view(null, null, true, null, queryBody, queryItemList(), authCondition);
    }

}
