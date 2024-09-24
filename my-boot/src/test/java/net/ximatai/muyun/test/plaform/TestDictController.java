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
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestDictController {

    String base = PlatformConst.BASE_PATH;

    @Test
    void testDictCategoryAdd() {
        given()
            .contentType("application/json")
            .body(Map.of(
                "id", "ROOT1",
                "v_name", "根",
                "v_remark", "备注"
            ))
            .when()
            .post("%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .body(is("ROOT1"));

        given()
            .contentType("application/json")
            .body(Map.of(
                "id", "ROOT2",
                "v_name", "根2",
                "v_remark", "备注"
            ))
            .when()
            .post("%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .body(is("ROOT2"));

        given()
            .contentType("application/json")
            .body(List.of(
                Map.of("id_at_app_dictcategory", "ROOT1",
                    "v_value", "01",
                    "v_name", "name1"
                ),
                Map.of("id_at_app_dictcategory", "ROOT1",
                    "v_value", "02",
                    "v_name", "name2"
                )
            ))
            .when()
            .post("%s/dict/update/%s/child/app_dict".formatted(base, "ROOT1"))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(
                Map.of(
                    "v_value", "03",
                    "v_name", "name3"
                )
            )
            .when()
            .post("%s/dict/update/%s/child/app_dict/create".formatted(base, "ROOT1"))
            .then()
            .statusCode(200);

        given()
            .contentType("application/json")
            .body(
                List.of(
                    Map.of(
                        "id_at_app_dictcategory", "ROOT2",
                        "v_value", "03",
                        "v_name", "name3"
                    )
                )
            )
            .when()
            .post("%s/dict/update/%s/child/app_dict".formatted(base, "ROOT2"))
            .then()
            .statusCode(200);

        List<TreeNode> response = given()
            .get("%s/dict/tree/%s".formatted(base, "ROOT1"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.size(), 3);

        List<TreeNode> response2 = given()
            .get("%s/dict/tree/%s".formatted(base, "ROOT2"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response2.size(), 1);

        String translateRes = given()
            .param("source", "01")
            .get("%s/dict/translate/%s".formatted(base, "ROOT1"))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("name1", translateRes);

        String res = given()
            .param("source", "02")
            .get("%s/dict/translate/%s".formatted(base, "ROOT2"))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(res.contains("类型中不存在"));
    }

}
