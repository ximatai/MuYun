package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@ApplicationScoped
public class RoleActionController extends ScaffoldForPlatform implements IChildAbility, IQueryAbility {

    @Inject
    DictCategoryController dictCategoryController;

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(
            new DictCategory("data_auth", "platform_dir", "系统数据权限", 1).setDictList(
                new Dict("open", "不限制"),
                new Dict("organization", "本机构"),
                new Dict("organization_and_subordinates", "本机构及下级"),
                new Dict("department", "本部门"),
                new Dict("department_and_subordinates", "本部门及下级"),
                new Dict("self", "本人"),
                new Dict("custom", "自定义")
            ), false);
    }

    @Override
    public String getMainTable() {
        return "auth_role_action";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("id_at_auth_role")
            .addColumn("id_at_app_module")
            .addColumn("v_alias_at_app_module", "冗余：模块别名")
            .addColumn("id_at_app_module_action")
            .addColumn("v_alias_at_app_module_action", "冗余：功能别名")
            .addColumn("dict_data_auth")
            .addColumn("v_custom_condition")
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
