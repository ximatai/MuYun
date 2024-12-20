package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MuYunException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.service.IAuthorizationService;
import net.ximatai.muyun.util.TreeBuilder;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Tag(name = "菜单方案管理")
@Path(BASE_PATH + "/menuSchema")
public class MenuSchemaController extends ScaffoldForPlatform implements IChildrenAbility, ISortAbility, IDataCheckAbility {

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    MenuController menuController;

    @Inject
    IAuthorizationService authorizationService;

    @Inject
    MuYunConfig config;

    @Override
    protected void afterInit() {
        super.afterInit();
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
        Set<String> roles = authorizationService.getUserAvailableRoles(userID);

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
            throw new MuYunException("未找到该用户匹配的菜单方案");
        }
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        List terminalType = (List) body.get("dicts_terminal_type");
        Objects.requireNonNull(terminalType, "终端类型必填");
        if (terminalType.isEmpty()) {
            throw new MuYunException("终端类型必填");
        }
    }

    @Override
    public void beforeDelete(String id) {
        super.beforeDelete(id);
        List<TreeNode> tree = this.tree(id);
        if (!tree.isEmpty()) {
            throw new MuYunException("该菜单方案对应菜单数据不为空，不允许删除");
        }
    }
}
