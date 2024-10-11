package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestModuleAndAction {
    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    void testModuleAndAction() {
        String moduleID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test"
            ))
            .when()
            .post("%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<Map> response = given()
            .header("userID", config.superUserId())
            .get("%s/module/view/%s/child/app_module_action".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(ModuleController.ACTIONS.size(), response.size());
    }

    @Test
    void testModuleAliasRepeat() {
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test2"
            ))
            .when()
            .post("%s/module/create".formatted(base))
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
            .post("%s/module/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(result.contains("已被使用，请勿再用"));
    }

    @Test
    void testModuleAliasRepeatForVoid() {
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "void"
            ))
            .when()
            .post("%s/module/create".formatted(base))
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
            .post("%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

    }

    @Test
    void testModuleCreateAndUpdate() {
        String moduleID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test3"
            ))
            .when()
            .post("%s/module/create".formatted(base))
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
            .post("%s/module/update/%s".formatted(base, moduleID))
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
            .post("%s/module/create".formatted(base))
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
            .post("%s/module/update/%s".formatted(base, moduleID))
            .then()
            .statusCode(500)
            .extract()
            .asString();

    }
}
