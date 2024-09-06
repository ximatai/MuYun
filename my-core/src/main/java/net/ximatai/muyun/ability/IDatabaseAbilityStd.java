package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.IDatabaseAccessStd;

public interface IDatabaseAbilityStd extends IDatabaseAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessStd getDatabase() {
        return (IDatabaseAccessStd) getDatabaseAccess();
    }
}
