package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/department")
public class DepartmentController extends ScaffoldForPlatform implements ITreeAbility, IChildAbility, IReferenceAbility, IDataCheckAbility {

    @Inject
    BaseBusinessTable base;

    @Inject
    Provider<OrganizationController> organizationProvider;

    @Inject
    DictController dictController;

    @Override
    public String getMainTable() {
        return "org_department";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(base.getTableWrapper())
            .addColumn("v_name", "名称")
            .addColumn("v_remark", "备注")
            .addColumn(Column.of("id_at_org_organization").setComment("所属机构").setNullable(false))
            .addColumn("dict_dept_type", "部门类型")
            .addColumn("id_at_auth_user__leader", "部门主管");
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            organizationProvider.get().toReferenceInfo("id_at_org_organization").autoPackage(),
            dictController.toReferenceInfo("dict_dept_type")
        );
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        if (body.get("id_at_org_organization") == null) {
            throw new MyException("部门必须归属具体机构，须包含 id_at_org_organization 数据");
        }
    }
}
