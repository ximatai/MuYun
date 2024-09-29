package net.ximatai.muyun.test.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String POSTGRES_IMAGE = "postgres:17-alpine";

    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE);

    @Override
    public Map<String, String> start() {
        POSTGRES.start();

        return Map.of(
            "quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl(),
            "quarkus.datasource.username", POSTGRES.getUsername(),
            "quarkus.datasource.password", POSTGRES.getPassword()
        );
    }

    @Override
    public void stop() {
        if (POSTGRES.isRunning()) {
            POSTGRES.stop();
        }
    }
}
