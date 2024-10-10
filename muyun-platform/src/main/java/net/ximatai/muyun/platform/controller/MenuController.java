package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;

import java.util.List;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/menu")
public class MenuController extends ScaffoldForPlatform implements ITreeAbility, IChildAbility, IReferenceAbility {

    @Inject
    ModuleController moduleController;

    @Inject
    DictCategoryController dictCategoryController;

    @Override
    public String getMainTable() {
        return "app_menu";
    }

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(
            new DictCategory("menu_opentype", "platform_dir", "菜单打开方式", 1).setDictList(
                new Dict("tab", "内嵌TAB"),
                new Dict("window", "新窗口")
            ), false);
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name")
            .addColumn("v_icon")
            .addColumn("v_remark")
            .addColumn("dict_menu_opentype")
            .addColumn("id_at_app_menu_schema")
            .addColumn("id_at_app_module")
            .addColumn(Column.of("b_enable").setDefaultValue(true))
            .addColumn(Column.of("b_homepage").setDefaultValue(false))
            .addIndex("id_at_app_menu_schema");

    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            moduleController.toReferenceInfo("id_at_app_module")
                .add("v_alias", "v_alias")
                .add("v_url", "v_url")
        );
    }
}
