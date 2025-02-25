package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.curd.std.ICustomSelectSqlAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.PlatformConst.DB_SCHEMA;

@Tag(description = "公告接收")
@Path(BASE_PATH + "/receive")
public class ReceiveController extends Scaffold implements ISelectAbility, IReferenceAbility, IQueryAbility, ICustomSelectSqlAbility {

    @Inject
    UserInfoController userInfoController;

    @Inject
    NoticeReadController noticeReadController;

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
        String userID = getUser().getId();
        Map<String, Object> view = IQueryAbility.super.view(id);
        if (view != null) {
            getDB().update("update %s.%s set i_views = i_views + 1 where id = ?".formatted(getSchemaName(), getMainTable()), id);
            PageResult result = noticeReadController.query(Map.of(
                "id_at_app_notice", id,
                "id_at_auth_user", userID
            ));
            if (result.getTotal() == 0) {
                noticeReadController.create(Map.of(
                    "id_at_app_notice", id,
                    "id_at_auth_user", userID
                ));
            }
        }
        return view;
    }

    @GET
    @Path("/unread_count")
    @Operation(summary = "查询当前登录人未读通知数量")
    public long unreadCount() {
        return this.query(Map.of("b_read", false)).getTotal();
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

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_title").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("b_read")
        );
    }

    @Override
    public String getCustomSql() {
        return """
            select *,
                   exists (select 1 from platform.app_notice_read
                                    where app_notice_read.id_at_app_notice = app_notice.id
                                    and app_notice_read.id_at_auth_user = '%s') as b_read
            from platform.app_notice
            """.formatted(getUser().getId());
    }
}
