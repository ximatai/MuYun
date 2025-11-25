package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@Startup
@ApplicationScoped
public class UserRoleController extends ScaffoldForPlatform implements IChildAbility, IQueryAbility {

    @Override
    public String getMainTable() {
        return "auth_user_role";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .addColumn(Column.of("id_at_auth_user").setNullable(false).setComment("用户id"))
            .addColumn(Column.of("id_at_auth_role").setNullable(false).setComment("角色id"))
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
