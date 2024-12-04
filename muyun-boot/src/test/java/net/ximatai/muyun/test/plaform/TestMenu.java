package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestMenu {

    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    @DisplayName("测试创建菜单架构时终端类型为空")
    void testTerminalTypeNull() {
        String error = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "默认方案"
            ))
            .when()
            .post("/api%s/menuSchema/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertEquals("终端类型必填", error);
    }

    @Test
    @DisplayName("测试创建菜单架构时终端类型为空列表")
    void testTerminalTypeBlank() {
        String error = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "默认方案",
                "dicts_terminal_type", List.of()
            ))
            .when()
            .post("/api%s/menuSchema/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertEquals("终端类型必填", error);
    }

    @Test
    @DisplayName("测试创建和更新菜单架构及其子菜单")
    void test() {
        String schemaID = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "默认方案",
                "dicts_terminal_type", List.of("web", "app")
            ))
            .when()
            .post("/api%s/menuSchema/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(schemaID);

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                List.of(
                    Map.of("v_name", "root1"),
                    Map.of("v_name", "root2")
                )
            )
            .when()
            .post("/api%s/menuSchema/update/%s/child/%s".formatted(base, schemaID, "app_menu"))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<TreeNode> menus = given()
            .header("userID", config.superUserId())
            .get("/api%s/menuSchema/tree/%s".formatted(base, schemaID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(2, menus.size());
        String root1ID = (String) menus.stream().filter(it -> it.getData().get("v_name").equals("root1")).findFirst().get().getData().get("id");
        String root2ID = (String) menus.stream().filter(it -> it.getData().get("v_name").equals("root2")).findFirst().get().getData().get("id");

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of("v_name", "1-1", "pid", root1ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("/api%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of("v_name", "1-2", "pid", root1ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("/api%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of("v_name", "2-1", "pid", root2ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("/api%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<TreeNode> menus2 = given()
            .header("userID", config.superUserId())
            .get("/api%s/menuSchema/tree/%s".formatted(base, schemaID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(2, menus2.size());
        assertEquals(2, menus2.getFirst().getChildren().size());
        assertEquals(1, menus2.getLast().getChildren().size());
    }
}
