package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.SortColumn;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@ApplicationScoped
public class ModuleActionController extends ScaffoldForPlatform implements IChildAbility {

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
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("id_at_app_module")
            .addColumn("v_name")
            .addColumn("v_alias")
            .addColumn("v_remark")
            .addColumn(Column.of("b_white").setDefaultValue(false))
            .addColumn("i_order")
            .addIndex(List.of("id_at_app_module", "v_alias"), true);
    }

}
