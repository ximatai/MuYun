package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.core.Scaffold;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/authorization")
public class AuthorizationController extends Scaffold implements IDatabaseAbilityStd {

    @Inject
    RoleActionController roleActionController;

    @GET
    @Path("/view")
    public List view(@QueryParam("roleID") String roleID, @QueryParam("moduleID") String moduleID) {
        return this.getDB().query("""
            select
                auth_role_action.id,
                app_module_action.id as id_at_app_module_action,
                app_module_action.v_name,
                app_module_action.id_at_app_module,
                app_module_action.i_order,
                auth_role_action.dict_data_auth,
                auth_role_action.v_custom_condition
            from platform.app_module_action
                     left join platform.auth_role_action
                         on auth_role_action.id_at_app_module_action = app_module_action.id
            and auth_role_action.id_at_auth_role = ?
            where app_module_action.id_at_app_module = ?
            order by app_module_action.i_order
            """, roleID, moduleID);
    }

    @GET
    @Path("/grant")
    public String grant(@QueryParam("roleID") String roleID, @QueryParam("actionID") String actionID) {
        return roleActionController.create(Map.of(
            "id_at_auth_role", roleID,
            "id_at_app_module_action", actionID
        ));
    }

    @GET
    @Path("/revoke/{id}")
    public Integer revoke(@PathParam("id") String id) {
        return roleActionController.delete(id);
    }

    @POST
    @Path("/setDataAuth/{id}")
    public Integer setDataAuth(@PathParam("id") String id, Map body) {
        return roleActionController.update(id, Map.of(
            "dict_data_auth", body.get("dict_data_auth"),
            "v_custom_condition", body.get("v_custom_condition")
        ));
    }

}
