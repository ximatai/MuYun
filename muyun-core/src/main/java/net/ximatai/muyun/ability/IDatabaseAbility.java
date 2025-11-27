package net.ximatai.muyun.ability;

import io.quarkus.arc.Arc;
import jakarta.enterprise.util.TypeLiteral;
import net.ximatai.muyun.database.core.IDatabaseOperations;

/**
 * 数据库操作能力
 */
public interface IDatabaseAbility {

    default IDatabaseOperations<String> getDatabaseOperations() {
        return Arc.container().instance(new TypeLiteral<IDatabaseOperations<String>>() {
        }).get();
    }

}
