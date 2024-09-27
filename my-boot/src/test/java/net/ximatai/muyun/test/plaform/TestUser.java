package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestUser {
    String base = PlatformConst.BASE_PATH;

    @Test
    void test() {
        // 新增人员信息
        String id = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "dict_user_gender", "0"
            ))
            .when()
            .post("%s/userinfo/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row = given()
            .get("%s/userinfo/view/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("测试", row.get("v_name"));
        assertFalse((Boolean) row.get("b_user"));

        // 设置用户
        given()
            .contentType("application/json")
            .body(Map.of(
                "v_username", "test",
                "v_password", "pw",
                "v_password2", "pw"
            ))
            .when()
            .post("%s/userinfo/setUser/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row2 = given()
            .get("%s/userinfo/view/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertTrue((Boolean) row2.get("b_user"));

        // 登录
        Map loginUser = given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw"
            ))
            .when()
            .post("/sso/login")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("test", loginUser.get("v_username_at_auth_user"));
        assertEquals("测试", loginUser.get("v_name"));

        // 停用用户
        given()
            .get("%s/userinfo/disableUser/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw"
            ))
            .when()
            .post("/sso/login")
            .then()
            .statusCode(500);

        // 启用用户
        given()
            .get("%s/userinfo/enableUser/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw"
            ))
            .when()
            .post("/sso/login")
            .then()
            .statusCode(200);

        // 删除用户
        given()
            .get("%s/userinfo/delete/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw"
            ))
            .when()
            .post("/sso/login")
            .then()
            .statusCode(500);

    }
}
