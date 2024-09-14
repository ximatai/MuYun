package net.ximatai.muyun.test.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.HashMap;
import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {
    private static final String POSTGRES_IMAGE = "postgres:16-alpine";

    private PostgreSQLContainer<?> postgres;

    @Override
    public Map<String, String> start() {
        if (postgres == null) {
            postgres = new PostgreSQLContainer<>(POSTGRES_IMAGE);
        }
        postgres.start();

        Map<String, String> conf = new HashMap<>();
        conf.put("quarkus.datasource.jdbc.url", postgres.getJdbcUrl());
        conf.put("quarkus.datasource.username", postgres.getUsername());
        conf.put("quarkus.datasource.password", postgres.getPassword());

        return conf;
    }

    @Override
    public void stop() {
        if (postgres != null && postgres.isRunning()) {
            postgres.stop();
        }
    }
}
