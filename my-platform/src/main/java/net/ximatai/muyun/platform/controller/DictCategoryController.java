package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/dictcategory")
public class DictCategoryController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility {

    @Inject
    DictController dictController;

    @Inject
    BaseBusinessTable base;

    @Override
    public String getMainTable() {
        return "app_dictcategory";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey("id")
            .setInherit(base.getTableWrapper())
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
