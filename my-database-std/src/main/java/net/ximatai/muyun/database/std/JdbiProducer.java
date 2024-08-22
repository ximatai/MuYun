package net.ximatai.muyun.database.std;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class JdbiProducer {

    @Inject
    AgroalDataSource dataSource;

    @Produces
    @ApplicationScoped
    public Jdbi createJdbi() {
        return Jdbi.create(dataSource);
    }

}
