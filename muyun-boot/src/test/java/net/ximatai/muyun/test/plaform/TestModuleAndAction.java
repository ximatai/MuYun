package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestModuleAndAction {
    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    @DisplayName("测试创建模块及其默认操作")
    void testModuleAndAction() {
        String moduleID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试1",
                "v_alias", "test_module_and_action"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<Map> response = given()
            .header("userID", config.superUserId())
            .get("/api%s/module/view/%s/child/app_module_action".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(ModuleAction.DEFAULT_ACTIONS.size(), response.size());
    }

    @Test
    @DisplayName("测试模块别名重复创建")
    void testModuleAliasRepeat() {
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test2"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test2"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(result.contains("已被使用，请勿再用"));
    }

    @Test
    @DisplayName("测试模块别名 'void' 允许重复创建")
    void testModuleAliasRepeatForVoid() {
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "void"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        // void 作为空标识关键字，允许重复
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "void"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

    }

    @Test
    @DisplayName("测试模块创建和更新别名")
    void testModuleCreateAndUpdate() {
        String moduleID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test3"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试2",
                "v_alias", "test3"
            ))
            .when()
            .post("/api%s/module/update/%s".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test4"
            ))
            .when()
            .post("/api%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试2",
                "v_alias", "test4"
            ))
            .when()
            .post("/api%s/module/update/%s".formatted(base, moduleID))
            .then()
            .statusCode(500)
            .extract()
            .asString();

    }
}
