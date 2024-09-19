package net.ximatai.muyun.ability.uni;

import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.uni.IDatabaseOperationsUni;

public interface IDatabaseUniAbility {

    IDatabaseOperations getDatabaseOperations();

    default IDatabaseOperationsUni getDatabase() {
        return (IDatabaseOperationsUni) getDatabaseOperations();
    }

}
