package net.ximatai.muyun.platform.controller;

import io.vertx.ext.web.RoutingContext;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IRuntimeAbility;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.util.TreeBuilder;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/runtime")
public class RuntimeController implements IRuntimeAbility {

    @Inject
    RoutingContext routingContext;

    @Inject
    MenuSchemaController menuSchemaController;

    @Inject
    MuYunConfig config;

    @Inject
    IDatabaseOperationsStd db;

    @GET
    @Path("/whoami")
    public IRuntimeUser whoami() {
        return getUser();
    }

    @GET
    @Path("/menu")
    public List<TreeNode> menu(@QueryParam("terminalType") String terminalType) {
        if (config.isSuperUser(whoami().getId())) {
            List<Map<String, Object>> list = db.query("""
                select id,pid,id as id_at_app_module,v_alias,v_name,v_url,'' as v_icon,'tab' as opentype from platform.app_module
                where b_system = true;
                """);

            return TreeBuilder.build("id", "pid", list, null, false, "v_name", null);
        } else {
            String schemaID = menuSchemaController.schemaForUser(whoami().getId(), terminalType);
            List<Map<String, Object>> list = db.query("""
                                    select app_menu.id,
                                           app_menu.pid,
                                           app_menu.id_at_app_module,
                                           app_module.v_alias,
                                           app_menu.v_name,
                                           app_module.v_url,
                                           app_menu.v_icon,
                                           app_menu.dict_menu_opentype as opentype
                                    from platform.app_menu
                                             left join platform.app_module on app_menu.id_at_app_module = app_module.id
                where app_menu.id_at_app_menu_schema = ?
                """, schemaID);

            return TreeBuilder.build("id", "pid", list, null, false, "v_name", null);
        }
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }
}
