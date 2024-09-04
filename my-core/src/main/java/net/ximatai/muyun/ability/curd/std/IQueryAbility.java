package net.ximatai.muyun.ability.curd.std;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface IQueryAbility extends ISelectAbility {

    List<QueryItem> queryItemList();

    @GET
    @Path("/queryFields")
    default List<QueryItem> queryFields() {
        return queryItemList().stream().filter(item -> !item.isHide()).collect(Collectors.toList());
    }

    @POST
    @Path("/view")
    default PageResult view(@QueryParam("page") int page,
                            @QueryParam("limit") int limit,
                            @QueryParam("orderField") String orderField,
                            @QueryParam("orderType") String orderType,
                            Map<String, Object> queryBody) {
        return ISelectAbility.super.view(page, limit, orderField, orderType, queryBody, queryItemList());
    }

}
