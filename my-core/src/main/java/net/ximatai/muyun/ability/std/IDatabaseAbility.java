package net.ximatai.muyun.ability.std;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.IDatabaseAccessStd;

public interface IDatabaseAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessStd getDatabase() {
        return (IDatabaseAccessStd) getDatabaseAccess();
    }
}
