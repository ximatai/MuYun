package net.ximatai.muyun.boot.db;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.Slf4JSqlLogger;

import javax.sql.DataSource;

@ApplicationScoped
public class JdbiProducer {

    @Inject
    DataSource dataSource;

    @Produces
    @ApplicationScoped
    public Jdbi createJdbi() {
        return Jdbi.create(dataSource)
            .setSqlLogger(new Slf4JSqlLogger());
    }

}
