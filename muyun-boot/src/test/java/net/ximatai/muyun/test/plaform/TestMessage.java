package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestMessage {
    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    @DisplayName("测试完整的发信、收信流程")
    void testMessageSend() {

        Map msg = Map.of(
            "v_title", "message",
            "v_context", "context",
            "app_message_person", List.of(
                Map.of(
                    "id_at_auth_user__to", config.superUserId()
                )
            )
        );

        String msgID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(msg)
            .when()
            .post("/api%s/outbox/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map inboxCount = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/unread_count".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(Map.class);

        Assertions.assertEquals(1, inboxCount.get("count"));

        PageResult outbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/outbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(1, outbox.getTotal());

        PageResult inbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(1, inbox.getTotal());

        given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/view/%s".formatted(base, msgID))
            .then()
            .statusCode(200);

        inboxCount = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/unread_count".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(Map.class);

        Assertions.assertEquals(0, inboxCount.get("count"));

        // 收件箱删除
        given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/delete/%s".formatted(base, msgID))
            .then()
            .statusCode(200);

        inbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(0, inbox.getTotal());

        // 收件箱删除不影响发件箱
        outbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/outbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(1, outbox.getTotal());

        // 发件箱删除
        given()
            .header("userID", config.superUserId())
            .get("/api%s/outbox/delete/%s".formatted(base, msgID))
            .then()
            .statusCode(200);

        outbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/outbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(0, outbox.getTotal());

        // 再发一封信，测试先删除发件箱
        msgID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(msg)
            .when()
            .post("/api%s/outbox/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        inbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(1, inbox.getTotal());

        // 发件箱删除
        given()
            .header("userID", config.superUserId())
            .get("/api%s/outbox/delete/%s".formatted(base, msgID))
            .then()
            .statusCode(200);

        inbox = given()
            .header("userID", config.superUserId())
            .get("/api%s/inbox/view".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        Assertions.assertEquals(0, inbox.getTotal());
    }
}
