package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.platform.model.MuYunMessage;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.platform.service.MessageCenter;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.NoticeController.MODULE_ALIAS;

@Startup
@Tag(description = "公告发布")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class NoticeController extends ScaffoldForPlatform implements IModuleRegisterAbility, IReferenceAbility {
    public final static String MODULE_ALIAS = "notice";

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    ModuleController moduleController;

    @Inject
    UserInfoController userInfoController;

    @Inject
    MessageCenter messageCenter;

    @Override
    public String getMainTable() {
        return "app_notice";
    }

    @Override
    protected void afterInit() {
        super.afterInit();
        dictCategoryController.putDictCategory(
            new DictCategory("dict_notice_access_scope", "platform_dir", "通知公开范围", 0).setDictList(
                new Dict("open", "全公开"),
                new Dict("organization", "按机构"),
                new Dict("department", "按部门")
            ), false);
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setComment("通知公告")
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_title", "标题")
            .addColumn("v_context", "内容")
            .addColumn("files_att", "附件")
            .addColumn("i_views", "浏览量", 0)
            .addColumn("dict_notice_access_scope", "公开范围")
            .addColumn("ids_at_org_organization", "公开机构范围")
            .addColumn("ids_at_org_department", "公开部门范围")
            .addColumn("b_release", "是否发布", false)
            .addColumn("t_release", "发布时间");
    }

    @GET
    @Path("/release/{id}")
    @Operation(summary = "发布公告")
    public int release(@PathParam("id") String id) {
        int updated = getDB().updateItem(getSchemaName(), getMainTable(), Map.of(
            "id", id,
            "b_release", true,
            "t_release", LocalDateTime.now()
        ));
        broadcast(id);
        return updated;
    }

    @GET
    @Path("/rollback/{id}")
    @Operation(summary = "撤销发布公告")
    public int rollback(@PathParam("id") String id) {
        return getDB().updateItem(getSchemaName(), getMainTable(), Map.of(
            "id", id,
            "b_release", false
        ));
    }

    @Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("通知发布")
            .setAlias(MODULE_ALIAS)
            .setTable(getMainTable())
            .setUrl("platform/notice/index")
            .addAction(new ModuleAction("release", "发布通知", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("rollback", "撤销发布", ModuleAction.TypeLike.UPDATE));
    }

    private void broadcast(String id) {
        Map<String, ?> map = view(id);
        String dictNoticeAccessScope = (String) map.get("dict_notice_access_scope");
        Set<String> users = switch (dictNoticeAccessScope) {
            case "open" ->
                getDB().query("select id from platform.auth_user where b_enabled = true").stream().map(it -> (String) it.get("id")).collect(Collectors.toSet());
            case "organization" -> getDB().query("""
                select auth_user.id
                from platform.auth_user
                         join platform.auth_userinfo on auth_user.id = auth_userinfo.id
                where b_enabled = true
                and id_at_org_organization = any(select ids_at_org_organization from platform.app_notice where id = ?)
                """, id).stream().map(it -> (String) it.get("id")).collect(Collectors.toSet());
            case "department" -> getDB().query("""
                select auth_user.id
                from platform.auth_user
                         join platform.auth_userinfo on auth_user.id = auth_userinfo.id
                where b_enabled = true
                and id_at_org_department = any(select ids_at_org_department from platform.app_notice where id = ?)
                """, id).stream().map(it -> (String) it.get("id")).collect(Collectors.toSet());
            default -> Set.of();
        };

        MuYunMessage message = new MuYunMessage(
            "收到新通知啦",
            "%s 刚刚发布了题为「%s」的通知".formatted(map.get("v_name_from"), map.get("v_title")),
            ""
        );

        users.forEach(it -> {
            messageCenter.send(it, message);
        });
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            userInfoController.toReferenceInfo("id_at_auth_user__create").add("v_name", "v_name_from")
        );
    }
}
