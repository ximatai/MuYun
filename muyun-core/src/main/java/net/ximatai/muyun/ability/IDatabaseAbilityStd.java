package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.core.IDatabaseOperations;

/**
 * 数据库操作能力（标准JDBC同步版）
 */
public interface IDatabaseAbilityStd extends IDatabaseAbility {
    default IDatabaseOperations<String> getDB() {
        return getDatabaseOperations();
    }
}
