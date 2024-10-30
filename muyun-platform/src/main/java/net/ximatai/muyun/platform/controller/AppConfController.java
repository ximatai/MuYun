package net.ximatai.muyun.platform.controller;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import java.util.Map;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Tag(name = "全局程序配置（面向前端平台级业务）")
@Path(BASE_PATH + "/conf")
public class AppConfController extends Scaffold implements ICreateAbility, ISelectAbility, IUpdateAbility, ITableCreateAbility {

    private final static String CONF_ID = "1";

    @Override
    public String getMainTable() {
        return "app_conf";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("j_conf");
    }

    @Override
    protected void afterInit() {
        super.afterInit();

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

}
