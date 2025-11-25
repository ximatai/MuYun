package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.DictCategory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static net.ximatai.muyun.platform.controller.AuthorizationController.DATA_AUTH_DICT;

@ApplicationScoped
public class RoleActionController extends ScaffoldForPlatform implements IChildAbility, IQueryAbility {

    @Inject
    DictCategoryController dictCategoryController;

    @Override
    protected void afterInit() {
        super.afterInit();
        dictCategoryController.putDictCategory(new DictCategory("data_auth", "platform_dir", "系统数据权限", 1).setDictList(DATA_AUTH_DICT), false);
        getDB().execute("""
            update %s set b_use  = true where b_use is null
            """.formatted(getSchemaDotTable()));
    }

    @Override
    public String getMainTable() {
        return "auth_role_action";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn("id_at_auth_role", "角色id")
            .addColumn("id_at_app_module", "模块id")
            .addColumn("v_alias_at_app_module", "冗余：模块别名")
            .addColumn("id_at_app_module_action", "模块id")
            .addColumn("v_alias_at_app_module_action", "冗余：功能别名")
            .addColumn("dict_data_auth", "数据权限字典")
            .addColumn("v_custom_condition", "自定义权限条件")
            .addColumn("b_use", "是否启用", "true")
            .addIndex("id_at_auth_role")
            .addIndex("id_at_app_module_action")
            .addIndex(List.of("id_at_auth_role", "id_at_app_module_action"), true)
            .addIndex(List.of("id_at_auth_role", "v_alias_at_app_module", "v_alias_at_app_module_action"), true);
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_auth_role"),
            QueryItem.of("id_at_app_module"),
            QueryItem.of("id_at_app_module_action")
        );
    }

    @Override
    public void fitOutDefaultValue(Map body) {
        super.fitOutDefaultValue(body);

        Objects.requireNonNull(body.get("id_at_auth_role"));
        Objects.requireNonNull(body.get("id_at_app_module_action"));

        Map<String, Object> action = getDB().row("select v_alias,id_at_app_module from platform.app_module_action where id = ?", body.get("id_at_app_module_action"));
        body.put("v_alias_at_app_module_action", action.get("v_alias"));
        body.put("id_at_app_module", action.get("id_at_app_module"));

        Map<String, Object> module = getDB().row("select v_alias from platform.app_module where id = ?", body.get("id_at_app_module"));
        body.put("v_alias_at_app_module", module.get("v_alias"));
    }
}
