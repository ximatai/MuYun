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
import net.ximatai.muyun.service.IAuthorizationService;
import net.ximatai.muyun.util.StringUtil;
import net.ximatai.muyun.util.TreeBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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

    @Inject
    IAuthorizationService authorizationService;

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

            List<TreeNode> treeNodes = TreeBuilder.build("id", "pid", list, null, false, "v_name", null);
            return filterMenuByAuth(treeNodes);
        }
    }

    @Override
    public RoutingContext getRoutingContext() {
        return routingContext;
    }

    private List<TreeNode> filterMenuByAuth(List<TreeNode> list) {
        String userID = whoami().getId();
        Map<String, Set<String>> authorizedResources = authorizationService.getAuthorizedResources(userID);

        return filterMenuByAuth(list, authorizedResources);
    }

    private List<TreeNode> filterMenuByAuth(List<TreeNode> list, Map<String, Set<String>> resources) {
        List<TreeNode> result = new ArrayList<>();
        for (TreeNode node : list) {
            Map data = node.getData();
            String alias = (String) data.get("v_alias");

            if (node.getChildren() != null && !node.getChildren().isEmpty()) { // 有孩子,先处理孩子
                List<TreeNode> children = filterMenuByAuth(node.getChildren(), resources);
                node.setChildren(children);
            }

            if (StringUtil.isNotBlank(data.get("id_at_app_module")) && !"void".equals(alias)) { // 说明配置了模块
                if (resources.containsKey(alias) && resources.get(alias).contains("menu")) { // 说明有 menu 授权
                    result.add(node);
                }
            } else if (node.getChildren() != null && !node.getChildren().isEmpty()) { // 没配置模块，就要检查是否有孩子，有孩子的话，也等同于有授权
                result.add(node);
            }
        }
        return result;
    }
}
