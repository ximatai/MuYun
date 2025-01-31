package net.ximatai.muyun.platform.controller;

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
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.util.StringUtil;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Tag(description = "部门管理")
@Path(BASE_PATH + "/department")
public class DepartmentController extends ScaffoldForPlatform implements ITreeAbility, IChildAbility, IReferableAbility, IReferenceAbility, IDataCheckAbility, IQueryAbility {

    @Inject
    Provider<OrganizationController> organizationProvider;
    @Inject
    Provider<UserInfoController> userInfoControllerProvider;

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
            .addColumn("id_at_auth_user__leader", "部门负责人")
            .addColumn("id_at_auth_user__boss", "主管领导");
    }

    @Override
    public String create(Map body) {
        check(body, false);

        String treePidName = Column.TREE_PID.getName();
        HashMap map = new HashMap<>(body);
        if (map.get(treePidName) == null) {
            map.put(treePidName, body.get("id_at_org_organization"));
        }
        return super.create(map);
    }

    @Override
    public List<TreeNode> tree(String rootID, Boolean showMe, String labelColumn, Integer maxLevel) {
        if (StringUtil.isBlank(rootID)) {
            rootID = getUser().getDepartmentId() != null ? getUser().getDepartmentId() : getUser().getOrganizationId();
        }
        if (showMe == null) {
            showMe = false;
        }
        return ITreeAbility.super.tree(rootID, showMe, labelColumn, maxLevel);
    }

    @Override
    public Integer sort(String id, String prevId, String nextId, String parentId) {
        if (StringUtil.isBlank(parentId)) {
            parentId = (String) view(id).get("id_at_org_organization");
        }

        return ITreeAbility.super.sort(id, prevId, nextId, parentId);
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            organizationProvider.get().toReferenceInfo("id_at_org_organization"),
            userInfoControllerProvider.get().toReferenceInfo("id_at_auth_user__leader").add("v_name", "v_leader_name"),
            userInfoControllerProvider.get().toReferenceInfo("id_at_auth_user__boss").add("v_name", "v_boss_name")
        );
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("id_at_org_organization")
        );
    }
}
