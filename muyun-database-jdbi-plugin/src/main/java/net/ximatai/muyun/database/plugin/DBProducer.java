package net.ximatai.muyun.database.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiDatabaseOperations;
import net.ximatai.muyun.database.jdbi.JdbiMetaDataLoader;
import net.ximatai.muyun.database.plugin.mapper.MyPgMapMapper;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class DBProducer {

    @Inject
    Jdbi jdbi;

    @Produces
    @ApplicationScoped
    public IDatabaseOperations<String> createDatabaseOperations() {
        JdbiMetaDataLoader loader = new JdbiMetaDataLoader(jdbi);
        JdbiDatabaseOperations<String> db = new JdbiDatabaseOperations<>(jdbi, loader, String.class, "id");

        db.setRowMapper(new MyPgMapMapper());
        return db;
    }

}
