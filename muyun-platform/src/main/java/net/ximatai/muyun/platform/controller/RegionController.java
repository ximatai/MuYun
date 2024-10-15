package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.platform.ScaffoldForPlatform;
import org.eclipse.microprofile.openapi.annotations.tags.Tag;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Startup
@Path(BASE_PATH + "/region")
@Tag(name = "行政区划管理")
public class RegionController extends ScaffoldForPlatform implements ITreeAbility, IReferableAbility {

    @Override
    public String getMainTable() {
        return "app_region";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_code", "区划代码")
            .addColumn("v_name", "区划名称")
            .addColumn("v_shortname", "区划简称")
            .addIndex("v_code", true);
    }

}
