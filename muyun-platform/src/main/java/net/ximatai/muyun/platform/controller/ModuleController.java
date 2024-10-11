package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.util.StringUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/module")
public class ModuleController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility, IReferableAbility, IDataCheckAbility, IQueryAbility {

    @Inject
    ModuleActionController moduleActionController;

    @Inject
    RoleActionController roleActionController;

    public static final List ACTIONS = List.of(
        Map.of("v_alias", "menu", "v_name", "菜单", "i_order", 0),

        Map.of("v_alias", "view", "v_name", "浏览", "i_order", 10),
        Map.of("v_alias", "export", "v_name", "导出", "i_order", 15),

        Map.of("v_alias", "create", "v_name", "新增", "i_order", 20),
//        Map.of("v_alias", "import", "v_name", "导入", "i_order", 25),

        Map.of("v_alias", "sort", "v_name", "排序", "i_order", 29),
        Map.of("v_alias", "update", "v_name", "修改", "i_order", 30),
        Map.of("v_alias", "delete", "v_name", "删除", "i_order", 40)
    );

    @Override
    public String getMainTable() {
        return "app_module";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name")
            .addColumn("v_alias", "模块标识，同后端Controller拦截入口名")
            .addColumn("v_url", "前端路径")
            .addColumn("v_remark")
            .addColumn("v_table")
            .addColumn(Column.of("b_system").setDefaultValue(false))
            .addColumn(Column.of("b_isolation").setComment("是否启用数据隔离").setDefaultValue(false))
            .addIndex("v_alias");
    }

    /**
     * 创建表成功后初始化相应数据
     *
     * @param isFirst
     */
    @Override
    public void onTableCreated(boolean isFirst) {
        if (isFirst) {
            initData();
        }
    }

    @Override
    @Transactional
    public String create(Map body) {
        String id = super.create(body);

        this.putChildTableList(id, "app_module_action", ACTIONS);

        return id;
    }

    @Override
    public void afterUpdate(String id) {
        getDB().update("""
            UPDATE platform.auth_role_action
            SET v_alias_at_app_module = (
              SELECT app_module.v_alias
              FROM platform.app_module
              WHERE platform.app_module.id = platform.auth_role_action.id_at_app_module
            )
            WHERE platform.auth_role_action.id_at_app_module = ?;
            """, id);
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            moduleActionController.toChildTable("id_at_app_module").setAutoDelete(),
            roleActionController.toChildTable("id_at_app_module").setAutoDelete()
        );
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        String alias = (String) body.get("v_alias");
        if (StringUtil.isBlank(alias)) {
            throw new MyException("请提供模块标识，即模块Controller拦截路径");
        }

        if (!"void".equals(alias)) {
            Map row = getDB().row("select * from platform.app_module where v_alias = ?", alias);
            if (row != null && !row.get("id").equals(body.get("id"))) {
                throw new MyException("模块标识[%s]已被使用，请勿再用".formatted(alias));
            }
        }
    }

    private void initData() {
        String root1 = this.createModule(null, "机构用户", "void", null, null);
        this.createModule(root1, "机构管理", "organization", "org_organization", "/just_a_demo");
        this.createModule(root1, "部门管理", "department", "org_department", "/just_a_demo");
        this.createModule(root1, "用户管理", "userinfo", "auth_userinfo", "/just_a_demo");

        String root2 = this.createModule(null, "平台管理", "void", null, null);

        String root21 = this.createModule(root2, "模块菜单", "void", null, null);
        this.createModule(root21, "模块管理", "module", "app_module", "/just_a_demo");
        this.createModule(root21, "菜单方案", "menuSchema", "app_menu_schema", "/just_a_demo");
        this.createModule(root21, "菜单管理", "menu", "app_menu", "/just_a_demo");

        String root22 = this.createModule(root2, "权限管理", "void", null, null);
        this.createModule(root22, "角色管理", "role", "app_module", "/just_a_demo");
        this.createModule(root22, "权限管理", "void", null, "/just_a_demo");

        String root23 = this.createModule(root2, "基础数据", "void", null, null);
        this.createModule(root23, "字典管理", "dict", "app_dictcategory", "/just_a_demo");
    }

    public String createModule(String pid, String name, String alias, String table, String url) {
        HashMap map = new HashMap();
        if (pid != null) {
            map.put("pid", pid);
        }

        map.put("v_name", name);
        map.put("v_alias", alias);
        map.put("v_table", table);
        map.put("v_url", url);
        map.put("b_system", true);
        return this.create(map);
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_alias")
        );
    }
}
