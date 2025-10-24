package net.ximatai.muyun.boot.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class DBProducer {

    @Inject
    Jdbi jdbi;

    @Produces
    @ApplicationScoped
    public IDatabaseOperations createDatabaseOperations() {
        JdbiMetaDataLoader loader = new JdbiMetaDataLoader(jdbi);
        JdbiDatabaseOperations db = new JdbiDatabaseOperations(jdbi, loader);

        return db;
    }

}
