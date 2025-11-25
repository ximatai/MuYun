package net.ximatai.muyun.base;

import io.quarkus.runtime.Startup;
import jakarta.inject.Singleton;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.builder.TableBase;
import net.ximatai.muyun.database.core.builder.TableWrapper;

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
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn("t_create")
            .addColumn("t_update")
            .addColumn("id_at_app_region") // 数据归属行政区划
            .addColumn("id_at_auth_user__create")
            .addColumn("id_at_auth_user__update")
            .addColumn("id_at_auth_user__perms") //权限所有人
            .addColumn("id_at_org_department__perms") //权限所有部门
            .addColumn("id_at_org_organization__perms") //权限所有机构
            .addColumn("id_at_app_module__perms"); //权限所有模块（对应：权限隔离）
    }

}
