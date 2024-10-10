package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.IDatabaseOperationsStd;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestModuleAndAction {
    String base = PlatformConst.BASE_PATH;

    @Inject
    IDatabaseOperationsStd databaseOperations;

    @BeforeEach
    void before() {
        databaseOperations.execute("truncate table platform.app_module");
        databaseOperations.execute("truncate table platform.app_module_action");
    }

    @Test
    void testModuleAndAction() {
        String moduleID = given()
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
            .get("%s/module/view/%s/child/app_module_action".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(7, response.size());
    }

    @Test
    void testModuleAliasRepeat() {
        given()
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

        String result = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test"
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

        given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试2",
                "v_alias", "test"
            ))
            .when()
            .post("%s/module/update/%s".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
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

        given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试2",
                "v_alias", "test2"
            ))
            .when()
            .post("%s/module/update/%s".formatted(base, moduleID))
            .then()
            .statusCode(500)
            .extract()
            .asString();

    }
}
