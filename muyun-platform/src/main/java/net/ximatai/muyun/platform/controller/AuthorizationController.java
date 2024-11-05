package net.ximatai.muyun.platform.controller;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.QueryParam;
import net.ximatai.muyun.ability.IDatabaseAbilityStd;
import net.ximatai.muyun.authorization.AuthorizationService;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.AuthorizationController.MODULE_ALIAS;

@Startup
@Tag(name = "权限管理")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class AuthorizationController extends Scaffold implements IDatabaseAbilityStd, IModuleRegisterAbility {
    public final static String MODULE_ALIAS = "authorization";

    private final LoadingCache<String, Map<String, Object>> actionCache = Caffeine.newBuilder()
        .build(this::loadAction);

    public static final List<Dict> DATA_AUTH_DICT = List.of(
        new Dict("open", "不限制"),
        new Dict("organization", "本机构"),
        new Dict("organization_and_subordinates", "本机构及下级"),
        new Dict("department", "本部门"),
        new Dict("department_and_subordinates", "本部门及下级"),
        new Dict("self", "本人"),
        new Dict("supervision_region", "监管区划"),
        new Dict("custom", "自定义")
    );

    @Inject
    RoleActionController roleActionController;

    @Inject
    ModuleController moduleController;

    @Inject
    AuthorizationService authorizationService;

    @Inject
    MuYunConfig muYunConfig;

    @GET
    @Path("/view")
    @Operation(summary = "查询某个角色针对某个功能的授权列表")
    public List view(@QueryParam("roleID") String roleID, @QueryParam("moduleID") String moduleID) {
        if (roleID == null) {
            roleID = "";
        }

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
    @Operation(summary = "授权")
    public String grant(@QueryParam("roleID") String roleID, @QueryParam("actionID") String actionID) {
        return roleActionController.create(Map.of(
            "id_at_auth_role", roleID,
            "id_at_app_module_action", actionID,
            "dict_data_auth", getDefaultDataAuth(actionID)
        ));
    }

    @GET
    @Path("/revoke/{id}")
    @Operation(summary = "撤回权限")
    public Integer revoke(@PathParam("id") String id) {
        return roleActionController.delete(id);
    }

    @POST
    @Path("/setDataAuth/{id}")
    @Operation(summary = "对已授权的数据修改授权数据范围")
    public Integer setDataAuth(@PathParam("id") String id, Map body) {
        String dataAuth = Objects.requireNonNull(body.get("dict_data_auth"), "必须提供数据授权范围字典内容").toString();

        String customCondition = (String) body.get("v_custom_condition");

        if ("custom".equals(dataAuth)) {
            if (StringUtil.isBlank(customCondition)) {
                throw new MyException("自定义数据权限必须提供自定义条件");
            }
        }

        Map<String, ?> roleActionMap = roleActionController.view(id);
        String actionID = (String) roleActionMap.get("id_at_app_module_action");

        if (!testDataAuth(actionID, dataAuth, customCondition)) {
            throw new MyException("数据权限不允许配置为：%s".formatted(dataAuth));
        }

        HashMap map = new HashMap(body);
        map.put("dict_data_auth", body.get("dict_data_auth"));
        if (body.containsKey("v_custom_condition")) {
            map.put("v_custom_condition", body.get("v_custom_condition"));
        }
        return roleActionController.update(id, map);
    }

    private String getDefaultDataAuth(String actionID) {
        Map<String, Object> actionMap = actionCache.get(actionID);
        String action = actionMap.get("v_alias_at_app_module_action").toString();

        if ("view".equals(action) && testDataAuth(actionID, "organization", null)) { // 默认查询
            return "organization";
        }

        return "open";
    }

    private boolean testDataAuth(String actionID, String dataAuth, String customCondition) {
        Map<String, Object> actionMap = actionCache.get(actionID);
        String module = actionMap.get("v_alias_at_app_module").toString();
        String tableName = actionMap.get("v_table").toString();

        String authCondition = null;
        if ("custom".equals(dataAuth)) {
            authCondition = customCondition;
        } else {
            authCondition = authorizationService.dictDataAuthToCondition(muYunConfig.superUserId(), module, dataAuth);
        }
        try {
            getDB().row("select 1 from %s where 1=1 and %s limit 1".formatted(tableName, authCondition));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Map<String, Object> loadAction(String actionID) {
        return getDB().row("""
            select app_module.v_table,
                   app_module.v_alias        as v_alias_at_app_module,
                   app_module_action.v_alias as v_alias_at_app_module_action
            from platform.app_module_action
                     join platform.app_module on app_module_action.id_at_app_module = app_module.id
            where app_module_action.id = ?""", actionID);
    }

    @Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("权限管理")
            .setAlias(MODULE_ALIAS)
            .setUrl("platform/authorization/index")
            .addAction(new ModuleAction("view", "浏览", ModuleAction.TypeLike.VIEW))
            .addAction(new ModuleAction("grant", "授权", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("revoke", "撤回权限", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("setDataAuth", "设置数据范围", ModuleAction.TypeLike.UPDATE));
    }
}
