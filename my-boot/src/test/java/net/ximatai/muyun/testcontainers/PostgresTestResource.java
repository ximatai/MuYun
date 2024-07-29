package net.ximatai.muyun.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
    PostgreSQLContainer<?> pgvector;

    @Override
    public Map<String, String> start() {
        pgvector = new PostgreSQLContainer<>("pgvector/pgvector:pg16");
        pgvector.start();

        Map<String, String> conf = new HashMap<>();
        conf.put("quarkus.datasource.jdbc.url", pgvector.getJdbcUrl());
        conf.put("quarkus.datasource.username", pgvector.getUsername());
        conf.put("quarkus.datasource.password", pgvector.getPassword());
        conf.put("quarkus.hibernate-orm.database.generation", "create");

        return conf;
    }

    @Override
    public void stop() {
        pgvector.stop();
    }
}
