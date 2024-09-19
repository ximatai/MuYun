package net.ximatai.muyun;

import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;

import java.util.List;

@Path("/demo")
public class MyDemoController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, ISortAbility, ITreeAbility, IChildrenAbility {
    @Override
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

    @Override
    public String getSchemaName() {
        return "public";
    }

    @Override
    public String getMainTable() {
        return "demo";
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("v_name")
        );
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of();
    }
}
