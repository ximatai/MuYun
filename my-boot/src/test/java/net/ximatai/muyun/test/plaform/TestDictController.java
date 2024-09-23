package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestDictController {

    @Test
    void testDictCategoryAdd() {
        given()
            .contentType("application/json")
            .body(Map.of(
                "id", "root1",
                "v_name", "根",
                "v_remark", "备注"
            ))
            .when()
            .post("/platform/dictcategory/create")
            .then()
            .statusCode(200)
            .body(is("root1"));

        given()
            .contentType("application/json")
            .body(Map.of(
                "id", "root2",
                "v_name", "根2",
                "v_remark", "备注"
            ))
            .when()
            .post("/platform/dictcategory/create")
            .then()
            .statusCode(200)
            .body(is("root2"));

        given()
            .contentType("application/json")
            .body(List.of(
                Map.of("id_at_app_dictcategory", "root1",
                    "v_value", "01",
                    "v_name", "name1"
                ),
                Map.of("id_at_app_dictcategory", "root1",
                    "v_value", "02",
                    "v_name", "name2"
                )
            ))
            .when()
            .post("/platform/dictcategory/update/%s/child/app_dict".formatted("root1"))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(
                Map.of("id_at_app_dictcategory", "root1",
                    "v_value", "03",
                    "v_name", "name3"
                )
            )
            .when()
            .post("/platform/dict/create/")
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(
                Map.of("id_at_app_dictcategory", "root2",
                    "v_value", "01",
                    "v_name", "name1"
                )
            )
            .when()
            .post("/platform/dict/create/")
            .then()
            .statusCode(200);

        List<TreeNode> response = given()
            .queryParam("rootID", "root1")
            .get("/platform/dict/tree")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.size(), 3);

        List<TreeNode> response2 = given()
            .queryParam("rootID", "root2")
            .get("/platform/dict/tree")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response2.size(), 1);
    }

}
