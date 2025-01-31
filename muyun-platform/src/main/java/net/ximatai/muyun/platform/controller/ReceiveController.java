package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.ReferenceInfo;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

@Tag(description = "公告接收")
@Path(BASE_PATH + "/receive")
public class ReceiveController extends Scaffold implements ISelectAbility, IReferenceAbility {

    @Inject
    UserInfoController userInfoController;

    @Override
    public String getMainTable() {
        return "app_notice";
    }

    @Override
    public String getSchemaName() {
        return DB_SCHEMA;
    }

    @Override
    public Map<String, Object> view(String id) {
        Map<String, Object> view = ISelectAbility.super.view(id);
        if (view != null) {
            getDB().update("update %s.%s set i_views = i_views + 1 where id = ?".formatted(getSchemaName(), getMainTable()), id);
        }
        return view;
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userInfoController.toReferenceInfo("id_at_auth_user__create")
        );
    }

    @Override
    public String getAuthCondition() {
        IRuntimeUser user = getUser();
        return """
             and b_release = true
             and (dict_notice_access_scope = 'open'
                 or (dict_notice_access_scope='organization' and '%s' = any(ids_at_org_organization))
                 or (dict_notice_access_scope='department' and '%s' = any(ids_at_org_department))
                 )
            """.formatted(user.getOrganizationId(), user.getDepartmentId());
    }
}
