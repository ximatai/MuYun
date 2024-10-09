package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.IDatabaseOperationsStd;

/**
 * 数据库操作能力（标准JDBC同步版）
 */
public interface IDatabaseAbilityStd extends IDatabaseAbility {

    IDatabaseOperations getDatabaseOperations();

    default IDatabaseOperationsStd getDB() {
        return (IDatabaseOperationsStd) getDatabaseOperations();
    }
}
