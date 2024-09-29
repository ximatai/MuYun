package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.IDatabaseOperationsStd;

public interface IDatabaseAbilityStd extends IDatabaseAbility {

    IDatabaseOperations getDatabaseOperations();

    default IDatabaseOperationsStd getDB() {
        return (IDatabaseOperationsStd) getDatabaseOperations();
    }
}
