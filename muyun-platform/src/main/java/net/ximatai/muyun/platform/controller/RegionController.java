package net.ximatai.muyun.platform.controller;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import static net.ximatai.muyun.platform.PlatformConst.BASE_PATH;

@Path(BASE_PATH + "/region")
public class RegionController extends ScaffoldForPlatform implements ITreeAbility {

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
            .addColumn("v_shortname", "区划简称");
    }

}
