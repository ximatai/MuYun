package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.Dict;
import net.ximatai.muyun.platform.model.DictCategory;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import org.eclipse.microprofile.openapi.annotations.Operation;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.time.LocalDateTime;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.NoticeController.MODULE_ALIAS;

@Tag(name = "公告发布")
@Path(BASE_PATH + "/"+MODULE_ALIAS)
public class NoticeController extends ScaffoldForPlatform implements IModuleRegisterAbility {
    public final static String MODULE_ALIAS = "notice";

    @Inject
    DictCategoryController dictCategoryController;

    @Inject
    ModuleController moduleController;

    @Override
    public String getMainTable() {
        return "app_notice";
    }

    @Override
    protected void afterInit() {
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
            .addColumn("file_att", "附件")
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
        return getDB().updateItem(getSchemaName(), getMainTable(), Map.of(
            "id", id,
            "b_release", true,
            "t_release", LocalDateTime.now()
        ));
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

    @java.lang.Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @java.lang.Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("通知发布")
            .setAlias(MODULE_ALIAS)
            .setTable(getMainTable())
            .setUrl("platform/notice/index")
            .addAction(new ModuleAction("release", "发布通知", ModuleAction.TypeLike.UPDATE))
            .addAction(new ModuleAction("rollback", "撤销发布", ModuleAction.TypeLike.UPDATE));
    }
}
