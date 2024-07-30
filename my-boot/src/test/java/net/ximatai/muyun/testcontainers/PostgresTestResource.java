package net.ximatai.muyun.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
    PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        postgres = new PostgreSQLContainer<>("postgres:16-alpine");
        postgres.start();

        Map<String, String> conf = new HashMap<>();
        conf.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        conf.put("quarkus.datasource.username", postgres.getUsername());
        conf.put("quarkus.datasource.password", postgres.getPassword());
        conf.put("quarkus.hibernate-orm.database.generation", "create");

        return conf;
    }

    @Override
    public void stop() {
        postgres.stop();
    }
}
