package net.ximatai.muyun.ability.uni;

import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.uni.IDatabaseAccessUni;

public interface IDatabaseUniAbility {

    IDatabaseAccess getDatabaseAccess();

    default IDatabaseAccessUni getDatabase() {
        return (IDatabaseAccessUni) getDatabaseAccess();
    }

}
