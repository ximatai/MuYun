package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestAppConf {

    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    @DisplayName("验证配置的获取和设置操作")
    void testGetAndSet() {

        Map conf = Map.of(
            "test", 1,
            "test2", List.of(1, 2, 3),
            "ok", true
        );

        Map confFromHTTP = given()
            .header("userID", config.superUserId())
            .when()
            .get("/api%s/conf/get".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertTrue(confFromHTTP.isEmpty());

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(conf)
            .when()
            .post("/api%s/conf/set".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        confFromHTTP = given()
            .header("userID", config.superUserId())
            .when()
            .get("/api%s/conf/get".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertFalse(confFromHTTP.isEmpty());
        assertEquals(conf.get("test"), confFromHTTP.get("test"));
        assertEquals(conf.get("test2"), confFromHTTP.get("test2"));
        assertEquals(conf.get("ok"), confFromHTTP.get("ok"));

    }

}
