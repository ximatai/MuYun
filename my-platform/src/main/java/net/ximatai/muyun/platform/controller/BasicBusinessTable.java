package net.ximatai.muyun.platform.controller;

import jakarta.inject.Singleton;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;

@Singleton
public class BasicBusinessTable extends Scaffold implements IMetadataAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "basic";
    }

    @Override
    public String getMainTable() {
        return "basic_business";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("t_create")
            .addColumn("t_update")
            .addColumn("t_delete")
            .addColumn("id_at_auth_user__create")
            .addColumn("id_at_auth_user__update")
            .addColumn("id_at_auth_user__delete")
            .addColumn("id_at_orga_department__create")
            .addColumn("id_at_orga_organization__create");
    }
}
