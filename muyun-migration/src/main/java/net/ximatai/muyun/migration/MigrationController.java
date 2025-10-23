package net.ximatai.muyun.migration;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICreateAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.ability.curd.std.IUpdateAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;

@Startup
@ApplicationScoped
public class MigrationController implements IMetadataAbility, ISelectAbility, ICreateAbility, IUpdateAbility {
    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public String getMainTable() {
        return "version";
    }

    @Override
    public String getSchemaName() {
        return "migration";
    }

    @PostConstruct
    private void init() {
        TableWrapper wrapper = new TableWrapper(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.of("id").setPrimaryKey().setType(ColumnType.VARCHAR))
            .addColumn(Column.of("i_version").setDefaultValue(0));

        new TableBuilder(databaseOperations).build(wrapper);
    }
}
