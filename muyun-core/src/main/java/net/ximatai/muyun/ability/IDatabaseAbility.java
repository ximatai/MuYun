package net.ximatai.muyun.ability;

import io.quarkus.arc.Arc;
import net.ximatai.muyun.database.IDatabaseOperations;

/**
 * 数据库操作能力
 */
public interface IDatabaseAbility {

    default IDatabaseOperations getDatabaseOperations() {
        return Arc.container().instance(IDatabaseOperations.class).get();
    }

}
