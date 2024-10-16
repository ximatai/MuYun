package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Tag(name = "模块管理")
@Path(BASE_PATH + "/module")
public class ModuleController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility, IReferableAbility, IDataCheckAbility, IQueryAbility, IDataBroadcastAbility {

    @Inject
    ModuleActionController moduleActionController;

    @Inject
    RoleActionController roleActionController;

    @Inject
    MuYunConfig muYunConfig;

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

    @Override
    protected void afterInit() {
        if (muYunConfig.debug()) {
            initData();
        }
    }

    @Override
    @Transactional
    public String create(Map body) {
        String id = super.create(body);
        if (body.get("app_module_action") == null) { //未提供功能信息，就自动创建默认信息
            this.putChildTableList(id, "app_module_action", ModuleAction.DEFAULT_ACTIONS.stream().map(ModuleAction::toMap).toList());
        }
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
        String root1 = this.lockModule(null, "机构用户", "void", null, null, null);
        this.lockModule(root1, "机构管理", "organization", "platform.org_organization", "platform/organization/index", null);
        this.lockModule(root1, "部门管理", "department", "platform.org_department", "platform/department/index", null);
        this.lockModule(root1, "用户管理", "userinfo", "platform.auth_userinfo", "platform/userinfo/index", null);

        String root2 = this.lockModule(null, "平台管理", "void", null, null, null);

        String root21 = this.lockModule(root2, "模块菜单", "void", null, null, null);
        this.lockModule(root21, "模块管理", "module", "platform.app_module", "platform/module/index", null);
        this.lockModule(root21, "菜单方案", "menuSchema", "platform.app_menu_schema", "platform/menuSchema/index", null);
        this.lockModule(root21, "菜单管理", "menu", "platform.app_menu", "platform/menu/index", null);

        String root22 = this.lockModule(root2, "权限管理", "void", null, null, null);
        this.lockModule(root22, "角色管理", "role", "platform.app_module", "platform/role/index", null);
        this.lockModule(root22, "权限管理", "authorization", null, "platform/authorization/index", null);

        String root23 = this.lockModule(root2, "基础数据", "void", null, null, null);
        this.lockModule(root23, "字典管理", "dict", "platform.app_dictcategory", "platform/dict/index", null);
        this.lockModule(root23, "行政区划", "region", "platform.app_region", "platform/region/index", null);
    }

    /**
     * 检查模块是否存在，不存在则创建
     *
     * @param pid
     * @param name
     * @param alias
     * @param table
     * @param url
     * @return
     */
    private String lockModule(String pid, String name, String alias, String table, String url, List<ModuleAction> moduleActions) {
        List<Map<String, Object>> list;

        if ("void".equals(alias)) { // 说明不是叶子节点
            list = getDB().query("select id from platform.app_module where b_system = true and v_alias = 'void' and v_name = ?", name);
        } else {
            list = getDB().query("select id from platform.app_module where b_system = true and v_alias = ?", alias);
        }

        String moduleID;
        List<Map> actions = null;

        if (moduleActions != null) {
            actions = moduleActions.stream().map(ModuleAction::toMap).toList();
        }

        if (list.isEmpty()) { // 新增模块
            HashMap map = new HashMap();
            if (pid != null) {
                map.put("pid", pid);
            }

            map.put("v_name", name);
            map.put("v_alias", alias);
            map.put("v_table", table);
            map.put("v_url", url);
            map.put("app_module_action", actions);
            map.put("b_system", true);
            moduleID = this.create(map);
        } else {
            moduleID = list.getFirst().get("id").toString();

            if (moduleActions != null) { // 修改时可以根据 moduleActions 进行增量补充
                List<Map> actionList = this.getChildTableList(moduleID, "app_module_action", null);
                Set<String> actionsInDB = actionList.stream().map(it -> it.get("v_alias").toString()).collect(Collectors.toSet());
                moduleActions.forEach(action -> {
                    if (!actionsInDB.contains(action.getAlias())) { // 说明对应功能并没有入库
                        this.createChild(moduleID, "app_module_action", action.toMap());
                    }
                });
            }
        }

        return moduleID;
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_alias")
        );
    }

    public void register(ModuleConfig config) {
        this.lockModule(null, config.getName(), config.getAlias(), config.getTable(), config.getUrl(), config.getActions());
    }
}
