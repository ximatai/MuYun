package net.ximatai.muyun.ability;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.model.ChildTable;

import java.util.List;

public interface IChildrenAbility {

    List<ChildTable> getChildren();

    @GET
    @Path("/view/{id}/children/{childTable}")
    default List getChildTableList(@PathParam("id") String id, @PathParam("childTable") String childTable) {
        ChildTable ct = getChild(childTable);
        return ct.getCtrl().view(null, null, true, null).getList();
    }

    default ChildTable getChild(String childTable) {
        return getChildren().stream().filter(c -> c.getCtrl().getMainTable().equals(childTable)).findFirst().orElseThrow();
    }

}
