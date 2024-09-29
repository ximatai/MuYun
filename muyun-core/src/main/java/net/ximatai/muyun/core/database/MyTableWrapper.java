package net.ximatai.muyun.core.database;

import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.database.builder.TableWrapper;

public class MyTableWrapper extends TableWrapper {

    public MyTableWrapper(IMetadataAbility metadataAbility) {
        super(metadataAbility.getMainTable());
        this.setSchema(metadataAbility.getSchemaName());
    }
}
