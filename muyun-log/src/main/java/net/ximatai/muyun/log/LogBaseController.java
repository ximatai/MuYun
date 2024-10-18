package net.ximatai.muyun.log;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.log.LogItem;

import java.util.List;

public abstract class LogBaseController implements IMetadataAbility, ISelectAbility, IQueryAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @Override
    public String getSchemaName() {
        return "log";
    }

    @PostConstruct
    protected void init() {

        TableWrapper wrapper = new TableWrapper(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("v_uri")
            .addColumn("v_method")
            .addColumn("j_params")
            .addColumn("v_host")
            .addColumn("v_useragent")
            .addColumn("v_os")
            .addColumn("v_browser")
            .addColumn("id_at_auth_user")
            .addColumn("v_username")
            .addColumn("v_module")
            .addColumn("v_action")
            .addColumn("v_data_id")
            .addColumn("i_cost")
            .addColumn("b_success")
            .addColumn("i_status_code")
            .addColumn("v_error")
            .addColumn("t_create");

        new TableBuilder(databaseOperations).build(wrapper);
    }

    public void log(LogItem logItem) {
        databaseOperations.insertItem(getSchemaName(), getMainTable(), logItem.toMap());
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_username"),
            QueryItem.of("t_create").setSymbolType(QueryItem.SymbolType.RANGE)
        );
    }

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }
}
