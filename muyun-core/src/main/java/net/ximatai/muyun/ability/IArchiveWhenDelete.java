package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.core.IDatabaseOperations;

import java.util.Map;
import java.util.Objects;

public interface IArchiveWhenDelete extends ITableCreateAbility {

    default String getArchiveTableName() {
        return getMainTable() + "__archive";
    }

    default void restore(String id) {
        IDatabaseOperations databaseOperations = this.getDatabaseOperations();
        Object item = databaseOperations.getItem(getSchemaName(), getArchiveTableName(), id);
        Objects.requireNonNull(item, "要恢复的数据ID[%s]不存在".formatted(id));

        databaseOperations.insertItem(getSchemaName(), getMainTable(), (Map<String, ?>) item);
        databaseOperations.deleteItem(getSchemaName(), getArchiveTableName(), id);
    }

}
