package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.MsgConfigController.MODULE_ALIAS;

@Startup
@Tag(description = "消息配置")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class MsgConfigController extends ScaffoldForPlatform implements ISortAbility, IModuleRegisterAbility, IQueryAbility {
    public final static String MODULE_ALIAS = "msg_config";

    @Inject
    ModuleController moduleController;

    @Override
    public String getMainTable() {
        return "app_msg_config";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setComment("消息配置")
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_title", "标题", null, false)
            .addColumn("v_alias", "别名", null, false)
            .addColumn("v_icon", "图标", null, false)
            .addColumn("v_home_page", "主页地址", null, false)
            .addColumn("v_content_page", "内容区域地址", null, false)
            .addColumn("v_api_count", "数量API", null, false)
            .addIndex("v_alias", true);
    }

    @Override
    protected void afterInit() {
        super.afterInit();

        PageResult result = this.query(Map.of("v_alias", "notice"));
        if (result.getTotal() == 0) {
            this.create(Map.of(
                "v_title", "通知公告",
                "v_alias", "notice",
                "v_icon", "",
                "v_home_page", "",
                "v_content_page", "",
                "v_api_count", ""
            ));
        }

    }

    @Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("消息配置")
            .setAlias(MODULE_ALIAS)
            .setTable(getMainTable())
            .setUrl("platform/conf/index")
            .addAction(new ModuleAction("set", "写入配置", ModuleAction.TypeLike.UPDATE));
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_alias")
        );
    }
}
