package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/department")
public class DepartmentController extends ScaffoldForPlatform implements ITreeAbility, IChildAbility, IReferableAbility, IReferenceAbility, IDataCheckAbility, IQueryAbility {

    @Inject
    Provider<OrganizationController> organizationProvider;

    @Override
    public String getMainTable() {
        return "org_department";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name", "名称")
            .addColumn("v_remark", "备注")
            .addColumn(Column.of("id_at_org_organization").setComment("所属机构").setNullable(false))
            .addColumn("id_at_auth_user__leader", "部门主管");
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            organizationProvider.get().toReferenceInfo("id_at_org_organization")
        );
    }

    @Override
    public void check(Map body, boolean isUpdate) {
        if (body.get("id_at_org_organization") == null) {
            throw new MyException("部门必须归属具体机构，须包含 id_at_org_organization 数据");
        }
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("id_at_org_organization")
        );
    }
}
