package net.ximatai.muyun.platform.controller;

import io.quarkus.runtime.Startup;
import jakarta.enterprise.context.ApplicationScoped;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.platform.ScaffoldForPlatform;

import java.util.List;

@Startup
@ApplicationScoped
public class NoticeReadController extends ScaffoldForPlatform implements IChildAbility, IQueryAbility {
    @Override
    public String getMainTable() {
        return "app_notice_read";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setComment("通知公告已读记录")
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .addColumn("id_at_app_notice")
            .addColumn("id_at_auth_user")
            .addColumn("t_create")
            .addIndex("t_create")
            .addIndex(List.of("id_at_app_notice", "id_at_auth_user"), true);
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id_at_app_notice"),
            QueryItem.of("id_at_auth_user")
        );
    }
}
