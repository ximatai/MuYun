package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;

import java.util.List;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/organization")
public class OrganizationController extends ScaffoldForPlatform implements ITreeAbility, IChildrenAbility, IReferableAbility, IReferenceAbility {

    @Inject
    BaseBusinessTable base;

    @Inject
    DictController dictController;

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    DepartmentController departmentController;

    @Override
    protected void afterInit() {
        dictCategoryController.putDictCategory(
            new DictCategory("org_type", "PLATFORM_DIR", "机构类型", 0).setDictList(List.of(
                new Dict("jituan", "集团公司"),
                new Dict("erjigongsi", "二级公司")
            )), true);
    }

    @Override
    public String getMainTable() {
        return "org_organization";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(base.getTableWrapper())
            .addColumn("v_remark", "备注")
            .addColumn("v_name", "名称")
            .addColumn("v_shortname", "简称")
            .addColumn("v_code", "编码")
            .addColumn("v_address", "地址")
            .addColumn("dict_org_type", "机构类型")
            .addColumn("v_introduction", "简介")
            .addColumn("v_contact_person", "联系人")
            .addColumn("v_contact_phone", "联系电话");
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(departmentController.toChildTable("id_at_org_organization"));
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            dictController.toReferenceInfo("dict_org_type")
        );
    }
}
