package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;

import java.util.List;

@Path("/platform/dictcategory")
public class DictCategoryController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility {

    @Inject
    DictController dictController;

    @Inject
    BasicBusinessTable basic;

    @Override
    public String getMainTable() {
        return "app_dictcategory";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey("id")
            .setInherit(basic.getTableWrapper())
            .addColumn("v_name")
            .addColumn("v_remark");
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            dictController.toChildTable("id_at_app_dictcategory")
        );
    }
}
