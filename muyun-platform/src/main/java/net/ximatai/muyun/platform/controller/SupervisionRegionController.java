package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

@ApplicationScoped
public class SupervisionRegionController extends ScaffoldForPlatform implements IChildAbility {

    @Override
    public String getMainTable() {
        return "org_organization_supervision";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("id_at_org_organization")
            .addColumn("id_at_app_region");
    }

}
