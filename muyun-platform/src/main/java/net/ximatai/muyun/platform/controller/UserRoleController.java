package net.ximatai.muyun.platform.controller;

import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.database.MyTableWrapper;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@ApplicationScoped
public class UserRoleController extends ScaffoldForPlatform implements IChildAbility, IQueryAbility {

    @Override
    public String getMainTable() {
        return "auth_user_role";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return new MyTableWrapper(this)
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("id_at_auth_user").setNullable(false))
            .addColumn(Column.of("id_at_auth_role").setNullable(false))
            .addIndex(List.of("id_at_auth_user", "id_at_auth_role"), true);
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_auth_user"),
            QueryItem.of("id_at_auth_role")
        );
    }
}
