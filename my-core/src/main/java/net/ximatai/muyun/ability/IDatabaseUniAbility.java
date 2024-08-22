package net.ximatai.muyun.ability;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.IDatabaseAccessUni;

public interface IDatabaseUniAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessUni getDatabase() {
        return (IDatabaseAccessUni) getDatabaseAccess();
    }

}
