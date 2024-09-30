package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestMenu {

    String base = PlatformConst.BASE_PATH;

    @Test
    void test() {
        String schemaID = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "默认方案",
                "dicts_terminal_type", List.of("web", "app")
            ))
            .when()
            .post("%s/menuSchema/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(schemaID);

        given()
            .contentType("application/json")
            .body(
                List.of(
                    Map.of("v_name", "root1"),
                    Map.of("v_name", "root2")
                )
            )
            .when()
            .post("%s/menuSchema/update/%s/child/%s".formatted(base, schemaID, "app_menu"))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<TreeNode> menus = given()
            .get("%s/menuSchema/tree/%s".formatted(base, schemaID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(2, menus.size());
        String root1ID = (String) menus.stream().filter(it -> it.getData().get("v_name").equals("root1")).findFirst().get().getData().get("id");
        String root2ID = (String) menus.stream().filter(it -> it.getData().get("v_name").equals("root2")).findFirst().get().getData().get("id");

        given()
            .contentType("application/json")
            .body(
                Map.of("v_name", "1-1", "pid", root1ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .contentType("application/json")
            .body(
                Map.of("v_name", "1-2", "pid", root1ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .contentType("application/json")
            .body(
                Map.of("v_name", "2-1", "pid", root2ID, "id_at_app_menu_schema", schemaID)
            )
            .when()
            .post("%s/menu/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<TreeNode> menus2 = given()
            .get("%s/menuSchema/tree/%s".formatted(base, schemaID))
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
