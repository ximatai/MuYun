package net.ximatai.muyun.database.std;

import io.agroal.api.AgroalDataSource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.std.argument.List2JsonArgumentFactory;
import net.ximatai.muyun.database.std.argument.Map2JsonArgumentFactory;
import org.jdbi.v3.core.Jdbi;

@ApplicationScoped
public class JdbiProducer {

    @Inject
    AgroalDataSource dataSource;

    @Produces
    @ApplicationScoped
    public Jdbi createJdbi() {
        return Jdbi.create(dataSource)
            .registerArgument(new Map2JsonArgumentFactory())
            .registerArgument(new List2JsonArgumentFactory());
//            .installPlugin(new PostgresPlugin())
//            .registerArrayType(String.class, "varchar")
//            .registerArgument(new PgArrayArgumentFactory())
//            .registerColumnMapper(new SqlArrayMapperFactory())
//            .registerColumnMapper(new PgArrayToListMapperFactory())

    }

}
