package net.ximatai.muyun.platform.controller;

import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.platform.ability.IModuleRegisterAbility;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;
import static net.ximatai.muyun.platform.controller.AppConfController.MODULE_ALIAS;

@Tag(description = "全局程序配置（面向前端平台级业务）")
@Path(BASE_PATH + "/" + MODULE_ALIAS)
public class AppConfController extends Scaffold implements ICreateAbility, ISelectAbility, IUpdateAbility, ITableCreateAbility, IModuleRegisterAbility {
    public final static String MODULE_ALIAS = "conf";

    private final static String CONF_ID = "1";

    @Inject
    ModuleController moduleController;

    @Override
    public String getMainTable() {
        return "app_conf";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn("j_conf");
    }

    @Override
    protected void afterInit() {
        super.afterInit();
        this.registerModule();

        Map<String, ?> view = this.view(CONF_ID);
        if (view == null) {
            this.create(Map.of(
                "id", CONF_ID,
                "j_conf", Map.of()
            ));
        }
    }

    @GET
    @Path("/get")
    public Map getConf() {
        Map conf = (Map) this.view(CONF_ID).get("j_conf");
        if (conf == null) {
            return Map.of();
        }
        return conf;
    }

    @POST
    @Path("/set")
    public Integer setConf(Map conf) {
        return this.update(CONF_ID, Map.of(
            "j_conf", conf
        ));
    }

    @Override
    public ModuleController getModuleController() {
        return moduleController;
    }

    @Override
    public ModuleConfig getModuleConfig() {
        return ModuleConfig.ofName("平台设置")
            .setAlias(MODULE_ALIAS)
            .setTable(getMainTable())
            .setUrl("platform/conf/index")
            .addAction(new ModuleAction("set", "写入配置", ModuleAction.TypeLike.UPDATE));
//            .addAction(new ModuleAction("get", "获取配置", ModuleAction.TypeLike.UPDATE));  不参与权限，所以注释掉
    }
}
