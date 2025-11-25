package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

@Startup
@ApplicationScoped
public class SupervisionRegionController extends ScaffoldForPlatform implements IChildAbility {

    @Override
    public String getMainTable() {
        return "org_organization_supervision";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("id_at_org_organization")
            .addColumn("id_at_app_region");
    }

}
