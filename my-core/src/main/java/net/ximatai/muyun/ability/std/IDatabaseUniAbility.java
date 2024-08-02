package net.ximatai.muyun.ability.std;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.IDatabaseAccessUni;

public interface IDatabaseUniAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessUni getDatabase() {
        return (IDatabaseAccessUni) getDatabaseAccess();
    }

}
