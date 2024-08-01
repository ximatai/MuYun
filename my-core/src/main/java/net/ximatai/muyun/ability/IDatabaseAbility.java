package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.IDatabaseAccessStandard;

public interface IDatabaseAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessStandard getDatabase() {
        return (IDatabaseAccessStandard) getDatabaseAccess();
    }
}
