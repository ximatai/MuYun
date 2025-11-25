package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.SortColumn;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@ApplicationScoped
public class ModuleActionController extends ScaffoldForPlatform implements IChildAbility, IChildrenAbility {

    @Inject
    RoleActionController roleActionController;

    @Override
    public SortColumn getDefatultSortColumn() {
        return new SortColumn("i_order");
    }

    @Override
    public String getMainTable() {
        return "app_module_action";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .addColumn("id_at_app_module", "模块id")
            .addColumn("v_name", "功能名称")
            .addColumn("v_alias", "功能标识")
            .addColumn("v_remark", "备注")
            .addColumn("b_white", "是否白名单", "false")
            .addColumn("i_order", "序号")
            .addIndex(List.of("id_at_app_module", "v_alias"), true);
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            roleActionController.toChildTable("id_at_app_module_action").setAutoDelete()
        );
    }

    @Override
    public void afterUpdate(String id) {
        getDB().update("""
            UPDATE platform.auth_role_action
            SET v_alias_at_app_module_action = (
                SELECT app_module_action.v_alias
                FROM platform.app_module_action
                WHERE platform.app_module_action.id = platform.auth_role_action.id_at_app_module_action
            )
            WHERE platform.auth_role_action.id_at_app_module_action = ?
            """, id);
    }
}
