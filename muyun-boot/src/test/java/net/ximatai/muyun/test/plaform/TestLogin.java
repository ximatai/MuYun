package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import io.restassured.response.Response;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.IRuntimeUser;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.checker.IExtraLoginChecker;
import net.ximatai.muyun.platform.controller.SsoController;
import net.ximatai.muyun.platform.controller.UserInfoController;
import net.ximatai.muyun.platform.model.RuntimeUser;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.time.LocalDate;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TestLogin {
    @Inject
    MuYunConfig config;

    @Inject
    UserInfoController userInfoController;

    String base = PlatformConst.BASE_PATH;

    String username = "testLogin";
    String password = "testpw1234";
    static String userID;

    @Inject
    SsoController ssoController;

    @Test
    @Order(1)
    void createUser() {
        // 新增人员信息
        userID = given()
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
            .get("/api%s/userinfo/view/%s".formatted(base, userID))
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
                "v_username", username,
                "v_password", password,
                "v_password2", password
            ))
            .when()
            .post("/api%s/userinfo/setUser/%s".formatted(base, userID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row2 = given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/view/%s".formatted(base, userID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertTrue((Boolean) row2.get("b_user"));

        // 登录
        Response response = given()
            .contentType("application/json")
            .body(Map.of(
                "username", username,
                "password", password,
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(200)
            .extract()
            .response();

        RuntimeUser loginUser = response.getBody().as(RuntimeUser.class);

        assertEquals(username, loginUser.getUsername());
        assertEquals("测试", loginUser.getName());

        String uid2 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "222"
            ))
            .when()
            .post("/api%s/userinfo/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_username", "testLoginUser2",
                "v_password", "testLoginUser2",
                "v_password2", "testLoginUser2"
            ))
            .when()
            .post("/api%s/userinfo/setUser/%s".formatted(base, uid2))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String uid3 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "333"
            ))
            .when()
            .post("/api%s/userinfo/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_username", "testLoginUser3",
                "v_password", "testLoginUser3",
                "v_password2", "testLoginUser3"
            ))
            .when()
            .post("/api%s/userinfo/setUser/%s".formatted(base, uid3))
            .then()
            .statusCode(200)
            .extract()
            .asString();
    }

    @Test
    @Order(2)
    void testLoginSuccess() {
        // 登录
        Response response = given()
            .contentType("application/json")
            .body(Map.of(
                "username", username,
                "password", password,
                "code", "muyun"
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(200)
            .extract()
            .response();
    }

    @Order(3)
    @Test
    void testLoginFailed() {
        String err = loginFailed(username, "wrongpw", "muyun");
        Assertions.assertEquals("用户名或密码错误，还有 2 次重试机会", err);

        String err2 = loginFailed(username, "wrongpw", "muyun");
        Assertions.assertEquals("用户名或密码错误，还有 1 次重试机会", err2);

        String err3 = loginFailed(username, "wrongpw", "muyun");
        Assertions.assertTrue(err3.contains("登录失败次数太多已被锁定"));

        String err4 = loginFailed(username, password, "muyun");
        Assertions.assertTrue(err4.contains("登录失败次数太多已被锁定"));
    }

    @Order(4)
    @Test
    void testLoginFailedWrongCode() {
        ssoController.unlockUser(username);

        String err = loginFailed(username, password, "muyun2");
        Assertions.assertEquals("验证码错误", err);

        String err2 = loginFailed(username, password, "muyun2");
        Assertions.assertEquals("验证码错误", err2);

        String err3 = loginFailed(username, password, "muyun2");
        Assertions.assertTrue(err3.contains("验证码错误"));
    }

    @Order(5)
    @Test
    void testLoginFailedWrongUsername() {
        String err = loginFailed(username + "x", "wrongpw", "muyun");
        Assertions.assertEquals("用户名或密码错误，还有 2 次重试机会", err);

        String err2 = loginFailed(username + "x", "wrongpw", "muyun");
        Assertions.assertEquals("用户名或密码错误，还有 1 次重试机会", err2);

        String err3 = loginFailed(username + "x", "wrongpw", "muyun");
        Assertions.assertTrue(err3.contains("登录失败次数太多已被锁定"));
    }

    @Order(6)
    @Test
    void testUserInvalidDaySetOK() {
        Map<String, Object> user = userInfoController.view(userID);

        java.sql.Date invalidDate = (java.sql.Date) user.get("d_invalid");

        assertEquals(LocalDate.now().plusDays(config.userValidateDays()), invalidDate.toLocalDate());
    }

    @Order(7)
    @Test
    void testUserInvalid() {

        given()
            .get("/api/platform/userinfo/setUserInvalid/%s?invalidDate=%s".formatted(userID, LocalDate.now().minusDays(1)))
            .then()
            .statusCode(200);

        String failed = loginFailed(username, password, "muyun");

        Assertions.assertTrue(failed.contains("该账号已超过有效期"));

    }

    private String loginFailed(String username, String password, String code) {
        return given()
            .contentType("application/json")
            .body(Map.of(
                "username", username,
                "password", password,
                "code", code
            ))
            .when()
            .post("/api/sso/login")
            .then()
            .statusCode(500)
            .extract()
            .asString();
    }

    @Test
    @Order(10)
    void deleteUser() {
        // 删除用户
        given()
            .header("userID", config.superUserId())
            .get("/api%s/userinfo/delete/%s".formatted(base, userID))
            .then()
            .statusCode(200);
    }

    @Test
    @Order(11)
    void testLoginBlockedByExtraChecker() {
        String err = loginFailed("testLoginUser2", "testLoginUser2", "muyun");
        Assertions.assertEquals("用户名 testLoginUser2 被禁止登录", err);

        String err2 = loginFailed("testLoginUser3", "testLoginUser3", "muyun");
        Assertions.assertEquals("用户名 testLoginUser3 被禁止登录", err2);
    }
}

@ApplicationScoped
class TestLoginUser2ForbidChecker implements IExtraLoginChecker {
    @Override
    public void check(IRuntimeUser runtimeUser) {
        if (runtimeUser.getUsername().equals("testLoginUser2")) {
            throw new RuntimeException("用户名 testLoginUser2 被禁止登录");
        }
    }
}

@ApplicationScoped
class TestLoginUser3ForbidChecker implements IExtraLoginChecker {
    @Override
    public void check(IRuntimeUser runtimeUser) {
        if (runtimeUser.getUsername().equals("testLoginUser3")) {
            throw new RuntimeException("用户名 testLoginUser3 被禁止登录");
        }
    }
}
