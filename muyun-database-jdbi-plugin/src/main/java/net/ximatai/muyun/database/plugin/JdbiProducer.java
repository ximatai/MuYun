package net.ximatai.muyun.database.plugin;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.plugin.argument.List2JsonArgumentFactory;
import net.ximatai.muyun.database.plugin.argument.Map2JsonArgumentFactory;
import net.ximatai.muyun.database.plugin.argument.PgArrayArgumentFactory;
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
            .setSqlLogger(new Slf4JSqlLogger())
            .registerArgument(new PgArrayArgumentFactory())
            .registerArgument(new Map2JsonArgumentFactory())
            .registerArgument(new List2JsonArgumentFactory());
//            .installPlugin(new PostgresPlugin())
//            .registerArrayType(String.class, "varchar")
//            .registerArgument(new PgArrayArgumentFactory())
//            .registerColumnMapper(new SqlArrayMapperFactory())
//            .registerColumnMapper(new PgArrayToListMapperFactory())

    }

}
