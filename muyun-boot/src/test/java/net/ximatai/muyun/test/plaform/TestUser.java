package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.controller.RoleController;
import net.ximatai.muyun.platform.model.RuntimeUser;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.apache.commons.codec.digest.DigestUtils;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestUser {
    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Inject
    RoleController roleController;

    @Test
    @DisplayName("测试用户创建、设置、登录、停用、启用、修改密码和删除功能")
    void test() {
        roleController.create(Map.of("id", "1"));
        roleController.create(Map.of("id", "2"));

        // 新增人员信息
        String id = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "dict_user_gender", "0"
            ))
            .when()
            .post("/api%s/userinfo/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row = given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/view/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("测试", row.get("v_name"));
        assertFalse((Boolean) row.get("b_user"));

        // 设置用户
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_username", "test",
                "v_password", "pw123456",
                "v_password2", "pw123456",
                "roles", List.of("1", "2")
            ))
            .when()
            .post("/api%s/userinfo/setUser/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row2 = given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/view/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertTrue((Boolean) row2.get("b_user"));

        List<String> roles = given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/roles/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertTrue(roles.contains("1"));
        assertTrue(roles.contains("2"));

        // 登录
        Response response = given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw123456",
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(200)
            .extract()
            .response();

        RuntimeUser loginUser = response.getBody().as(RuntimeUser.class);
        response.getCookies();

        assertEquals("test", loginUser.getUsername());
        assertEquals("测试", loginUser.getName());

        given()
            .cookies(response.getCookies())
            .get("/api%s/runtime/whoami".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        // 停用用户
        given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/disableUser/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw123456",
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(500);

        // 启用用户
        given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/enableUser/%s".formatted(base, id))
            .then()
            .statusCode(200);

        // 修改密码
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_password", "1234567z",
                "v_password2", "1234567z"
            ))
            .when()
            .post("/api%s/userinfo/setPassword/%s".formatted(base, id))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        // 修改密码（自助）
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_old_password", "pw2",
                "v_password", "pw3",
                "v_password2", "pw3"
            ))
            .when()
            .post("/api%s/userinfo/setPasswordSelf/%s".formatted(base, id))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw3",
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(500);

        // 删除用户
        given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/delete/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(Map.of(
                "username", "test",
                "password", "pw",
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(500);

    }

    @Test
    @DisplayName("测试密码加密功能")
    void testEncryptPassword() {
        String encryptPassword = encryptPassword("admin@bys", "-3");
        assertEquals("FFE9AEB76F57D8BC562D55022DA8C2DB", encryptPassword);
    }

    private String encryptPassword(String password, String code) {
        String md5Password = DigestUtils.md5Hex(password).toUpperCase();
        return DigestUtils.md5Hex(md5Password + code).toUpperCase();
    }
}
