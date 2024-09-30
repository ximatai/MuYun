package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.service.IAuthorizationService;
import net.ximatai.muyun.util.TreeBuilder;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/menuSchema")
public class MenuSchemaController extends ScaffoldForPlatform implements IChildrenAbility, ISortAbility {

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    MenuController menuController;

    @Inject
    IAuthorizationService authorizationService;

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(
            new DictCategory("terminal_type", "muyun_dir", "终端类型", 1).setDictList(
                new Dict("web", "网页端"),
                new Dict("app", "手机端"),
                new Dict("wx", "微信端")
            ), false
        );
    }

    @Override
    public String getMainTable() {
        return "app_menu_schema";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("v_name")
            .addColumn("ids_at_auth_role")
            .addColumn("ids_at_orga_organization")
            .addColumn("dicts_terminal_type");
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            menuController.toChildTable("id_at_app_menu_schema")
        );
    }

    @GET
    @Path("/tree/{id}")
    public List<TreeNode> tree(@PathParam("id") String id) {
        List menus = getChildTableList(id, "app_menu", null);
        return TreeBuilder.build("id", "pid", menus, null, false, "v_name", null);
    }

    @GET
    @Path("/schemaForUser")
    public String schemaForUser(@QueryParam("userID") String userID, @QueryParam("terminalType") String terminalType) {
        if (terminalType == null) {
            terminalType = "web";
        }
        List<String> roles = authorizationService.getUserAvailableRoles(userID);

        Map row = getDB().row("""
            select * from platform.app_menu_schema
            where ? = any(dicts_terminal_type)
            and ids_at_auth_role && ?
            order by n_order limit 1
            """, terminalType, roles.toArray(new String[0]));

        if (row != null) {
            return row.get("id").toString();
        }

        row = getDB().row("""
            select * from platform.app_menu_schema
            where ? = any(dicts_terminal_type)
            and (select id_at_org_organization from platform.auth_userinfo where id = ?) = any(ids_at_orga_organization)
            order by n_order limit 1
            """, terminalType, userID);

        if (row != null) {
            return row.get("id").toString();
        } else {
            throw new MyException("未找到与用户[%s]匹配的菜单方案");
        }
    }
}
