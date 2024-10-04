package net.ximatai.muyun.base;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBase;
import net.ximatai.muyun.database.builder.TableWrapper;

@Startup(1000)
@Singleton
public class BaseBusinessTable extends Scaffold implements ITableCreateAbility {

    public final static String SCHEMA_NAME = "base";
    public final static String TABLE_NAME = "base_business";
    public final static TableBase TABLE = new TableBase(SCHEMA_NAME, TABLE_NAME);

    @Override
    public String getSchemaName() {
        return SCHEMA_NAME;
    }

    @Override
    public String getMainTable() {
        return TABLE_NAME;
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("t_create")
            .addColumn("t_update")
            .addColumn("id_at_auth_user__create")
            .addColumn("id_at_auth_user__update")
            .addColumn("id_at_org_department__create")
            .addColumn("id_at_org_organization__create");
    }

}
