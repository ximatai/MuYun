package net.ximatai.muyun.test.testcontainers;

import io.quarkus.test.common.QuarkusTestResourceLifecycleManager;
import org.testcontainers.containers.PostgreSQLContainer;

import java.util.Map;

public class PostgresTestResource implements QuarkusTestResourceLifecycleManager {

    private static final String POSTGRES_IMAGE = "postgres:18.1-alpine";

    // 使用静态变量确保所有测试类共享一个实例
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(POSTGRES_IMAGE).withReuse(true);

    @Override
    public Map<String, String> start() {
        if (!POSTGRES.isRunning()) {
            POSTGRES.start();
        }
        return Map.of(
            "quarkus.datasource.jdbc.url", POSTGRES.getJdbcUrl(),
            "quarkus.datasource.username", POSTGRES.getUsername(),
            "quarkus.datasource.password", POSTGRES.getPassword()
        );
    }

    @Override
    public void stop() {
        // 不停止容器，让其他测试类可以继续使用
    }
}
