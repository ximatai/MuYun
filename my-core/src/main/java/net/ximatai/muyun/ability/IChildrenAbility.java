package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface IChildrenAbility {

    List<ChildTableInfo> getChildren();

    @GET
    @Path("/view/{id}/children/{childTable}")
    default List getChildTableList(@PathParam("id") String id, @PathParam("childTable") String childTable, @QueryParam("sort") List<String> sort) {
        ChildTableInfo ct = getChildTable(childTable);
        String foreignKey = ct.getForeignKey();
        return ct.getCtrl().view(null, null, true, sort, Map.of(foreignKey, id), List.of(QueryItem.of(foreignKey))).getList();
    }

    @GET
    @Path("/view/{id}/children/{childTable}/view/{childId}")
    default Map<String, ?> getChild(@PathParam("id") String id, @PathParam("childTable") String childTable, @PathParam("childId") String childId) {
        ChildTableInfo ct = getChildTable(childTable);
        Map<String, ?> child = ct.getCtrl().view(childId);
        if (!child.get(ct.getForeignKey()).equals(id)) {
            throw new RuntimeException("child id not match");
        }
        return child;
    }

    @POST
    @Path("/update/{id}/children/{childTable}/create")
    default String createChild(@PathParam("id") String id, @PathParam("childTable") String childTable, Map body) {
        ChildTableInfo ct = getChildTable(childTable);
        String foreignKey = ct.getForeignKey();
        Map map = new HashMap(body);
        map.put(foreignKey, id);
        return ct.getCtrl().create(map);
    }

    @POST
    @Path("/update/{id}/children/{childTable}/update/{childId}")
    default Integer updateChild(@PathParam("id") String id, @PathParam("childTable") String childTable, @PathParam("childId") String childId, Map body) {
        getChild(id, childTable, childId);
        ChildTableInfo ct = getChildTable(childTable);
        return ct.getCtrl().update(childId, body);
    }

    @GET
    @Path("/update/{id}/children/{childTable}/delete/{childId}")
    default Integer deleteChild(@PathParam("id") String id, @PathParam("childTable") String childTable, @PathParam("childId") String childId) {
        getChild(id, childTable, childId);
        ChildTableInfo ct = getChildTable(childTable);
        return ct.getCtrl().delete(childId);
    }

    default ChildTableInfo getChildTable(String childTable) {
        return getChildren().stream().filter(c -> c.getCtrl().getMainTable().equals(childTable)).findFirst().orElseThrow();
    }

}
