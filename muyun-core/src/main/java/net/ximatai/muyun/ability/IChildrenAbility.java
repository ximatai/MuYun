package net.ximatai.muyun.ability;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.model.BatchResult;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.parameters.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 关联子表的能力
 */
public interface IChildrenAbility {

    List<ChildTableInfo> getChildren();

    @GET
    @Path("/view/{id}/child/{childAlias}")
    @Operation(summary = "查询指定子表的数据列表")
    default List<Map> getChildTableList(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, @QueryParam("sort") List<String> sort) {
        ChildTableInfo ct = getChildTable(childAlias);
        String foreignKey = ct.getForeignKey();
        return ct.getCtrl().view(null, null, true, sort, Map.of(foreignKey, id), List.of(QueryItem.of(foreignKey))).getList();
    }

    @GET
    @Path("/view/{id}/child/{childAlias}/view/{childId}")
    @Operation(summary = "查询指定子表的一条数据")
    default Map<String, ?> getChild(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, @Parameter(description = "子表id") @PathParam("childId") String childId) {
        ChildTableInfo ct = getChildTable(childAlias);
        Map<String, ?> child = ct.getCtrl().view(childId);
        if (child != null && !child.get(ct.getForeignKey()).equals(id)) {
            throw new RuntimeException("子表数据匹配不到当前主表");
        }
        return child;
    }

    @POST
    @Path("/update/{id}/child/{childAlias}")
    @Transactional
    @Operation(summary = "覆盖式更新对应子表数据")
    default BatchResult putChildTableList(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, List body) {
        ChildTableInfo ct = getChildTable(childAlias);
        String childPK = ct.getCtrl().getPK();

        body.forEach(item -> {
            if (item instanceof Map map) {
                if (map.containsKey(ct.getForeignKey())
                    && map.get(ct.getForeignKey()) != null
                    && !map.get(ct.getForeignKey()).equals(id)) { // 说明存在要编辑的数据不是当前主表的对应子表
                    throw new RuntimeException("子表数据超出对应主表管辖范围，请检查「%s」的内容".formatted(ct.getForeignKey()));
                }
            }
        });

        int update = 0;
        int create = 0;
        int delete = 0;

        List<String> idsAlreadyHere = new ArrayList<>(getChildTableList(id, childAlias, null).stream().map(it -> it.get(childPK).toString()).toList());

        System.out.println(idsAlreadyHere);

        for (Object o : body) {
            if (o instanceof Map map) {
                String childId = (String) map.get(ct.getCtrl().getPK());
                if (childId != null && idsAlreadyHere.contains(childId)) { //说明有id，走编辑
                    ct.getCtrl().update(childId, map);
                    idsAlreadyHere.remove(childId);
                    update++;
                } else { //新增
                    Map newMap = new HashMap(map);
                    newMap.put(ct.getForeignKey(), id);
                    String newId = ct.getCtrl().create(newMap);
                    idsAlreadyHere.remove(newId);
                    create++;
                }
            }
        }

        for (String s : idsAlreadyHere) {
            delete += ct.getCtrl().delete(s);
        }

        return new BatchResult(create, update, delete);
    }

    @POST
    @Path("/update/{id}/child/{childAlias}/create")
    @Operation(summary = "新增一条对应子表的数据")
    default String createChild(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, Map body) {
        ChildTableInfo ct = getChildTable(childAlias);
        String foreignKey = ct.getForeignKey();
        Map map = new HashMap(body);
        map.put(foreignKey, id);
        return ct.getCtrl().create(map);
    }

    @POST
    @Path("/update/{id}/child/{childAlias}/batchCreate")
    @Operation(summary = "新增多条对应子表的数据")
    default List<String> batchCreate(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, List<Map> list) {
        ChildTableInfo ct = getChildTable(childAlias);
        String foreignKey = ct.getForeignKey();
        List<Map> newList = list.stream().map(it -> {
            Map map = new HashMap(it);
            map.put(foreignKey, id);
            return map;
        }).toList();

        return ct.getCtrl().batchCreate(newList);
    }

    @POST
    @Path("/update/{id}/child/{childAlias}/update/{childId}")
    @Operation(summary = "修改一条对应子表的数据")
    default Integer updateChild(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, @Parameter(description = "子表id") @PathParam("childId") String childId, Map body) {
        getChild(id, childAlias, childId);
        ChildTableInfo ct = getChildTable(childAlias);
        return ct.getCtrl().update(childId, body);
    }

    @GET
    @Path("/update/{id}/child/{childAlias}/delete/{childId}")
    @Operation(summary = "删除一条对应子表的数据")
    default Integer deleteChild(@Parameter(description = "主表id") @PathParam("id") String id, @Parameter(description = "子表别名") @PathParam("childAlias") String childAlias, @Parameter(description = "子表id") @PathParam("childId") String childId) {
        getChild(id, childAlias, childId);
        ChildTableInfo ct = getChildTable(childAlias);
        return ct.getCtrl().delete(childId);
    }

    default ChildTableInfo getChildTable(String childAlias) {
        return getChildren().stream().filter(c -> c.getChildAlias().equals(childAlias)).findFirst().orElseThrow();
    }

}
