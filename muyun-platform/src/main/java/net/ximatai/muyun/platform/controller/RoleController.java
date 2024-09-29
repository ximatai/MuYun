package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/role")
public class RoleController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility {

    @Inject
    UserRoleController userRoleController;

    private ChildTableInfo userRoleChild;

    @Override
    protected void afterInit() {
        userRoleChild = userRoleController.toChildTable("id_at_auth_role");
    }

    @Override
    public String getMainTable() {
        return "auth_role";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name")
            .addColumn("v_remark");
    }

    @Override
    public List<ChildTableInfo> getChildren() {

        return List.of(
            userRoleChild.setAutoDelete()
        );
    }

    @GET
    @Path("/assign/{roleID}/to/{userID}")
    public String assign(@PathParam("roleID") String roleID, @PathParam("userID") String userID) {
        try {
            return this.createChild(roleID, userRoleChild.getChildAlias(), Map.of(
                "id_at_auth_user", userID
            ));
        } catch (Exception ignored) {
            return "";
        }
    }

    @GET
    @Path("/revoke/{roleID}/to/{userID}")
    public Integer revoke(@PathParam("roleID") String roleID, @PathParam("userID") String userID) {
        PageResult<Map> result = userRoleController.query(Map.of(
            "id_at_auth_user", userID,
            "id_at_auth_role", roleID
        ));

        if (result.getSize() == 1) {
            String id = (String) result.getList().get(0).get("id");
            return this.deleteChild(roleID, userRoleChild.getChildAlias(), id);
        }

        return 1;
    }
}
