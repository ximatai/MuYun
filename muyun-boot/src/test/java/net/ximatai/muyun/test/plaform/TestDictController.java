package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.platform.model.DictTreeNode;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestDictController {
    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    @DisplayName("测试获取字典分类树结构")
    void testDictCategoryTree() {
        List<TreeNode> response = given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/tree".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertFalse(response.isEmpty());
    }

    @Test
    @DisplayName("测试获取空的字典分类")
    void testVoidDictCategory() {
        List<DictTreeNode> response = given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/tree/%s".formatted(base, UUID.randomUUID().toString()))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertTrue(response.isEmpty());
    }

    @Test
    @DisplayName("测试添加字典分类")
    void testDictCategoryAdd() {
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", "root1",
                "v_name", "根",
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .body(is("root1"));

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", "root2",
                "v_name", "根2",
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .body(is("root2"));

        given()
            .header("userID", config.superUserId())
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
            .post("/api%s/dict/update/%s/child/app_dict".formatted(base, "root1"))
            .then()
            .statusCode(200);

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of(
                    "v_value", "03",
                    "v_name", "name3"
                )
            )
            .when()
            .post("/api%s/dict/update/%s/child/app_dict/create".formatted(base, "root1"))
            .then()
            .statusCode(200);

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of(
                    "v_value", "03",
                    "v_name", "name3"
                )
            )
            .when()
            .post("/api%s/dict/update/%s/child/app_dict/create".formatted(base, "root1"))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(result.contains("该类目下存在相同的字典值"));

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                List.of(
                    Map.of(
                        "id_at_app_dictcategory", "root2",
                        "v_value", "03",
                        "v_name", "name3"
                    )
                )
            )
            .when()
            .post("/api%s/dict/update/%s/child/app_dict".formatted(base, "root2"))
            .then()
            .statusCode(200);

        List<DictTreeNode> response = given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/tree/%s".formatted(base, "root1"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.size(), 3);
        DictTreeNode node = response.get(0);
        assertNotNull(node.getValue());

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(
                Map.of(
                    "v_value", "031",
                    "v_name", "name31",
                    "pid", node.getId()
                )
            )
            .when()
            .post("/api%s/dict/update/%s/child/app_dict/create".formatted(base, "root1"))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<Map> responsex = given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/tree/%s".formatted(base, "root1"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(responsex.size(), 3);
        Map nodex = ((List<Map>) responsex.get(0).get("children")).get(0);
        assertNotNull(nodex.get("value"));

        List<DictTreeNode> response2 = given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/tree/%s".formatted(base, "root2"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response2.size(), 1);

        String translateRes = given()
            .header("userID", config.superUserId())
            .param("source", "01")
            .get("/api%s/dict/translate/%s".formatted(base, "root1"))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("name1", translateRes);

        String res = given()
            .header("userID", config.superUserId())
            .param("source", "02")
            .get("/api%s/dict/translate/%s".formatted(base, "root2"))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(res.contains("类型中不存在"));
    }

    @Test
    @DisplayName("测试添加重复ID的字典分类")
    void testDictCategoryAddDuplicateID() {

        String id = UUID.randomUUID().toString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", id,
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200);

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", id,
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(result.contains("存在重复的数据字典类目编码"));
    }

    @Test
    @DisplayName("测试添加重复名称的字典分类")
    void testDictCategoryAddDuplicateName() {
        String name = UUID.randomUUID().toString();
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", UUID.randomUUID().toString(),
                "v_name", name,
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200);

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", UUID.randomUUID().toString(),
                "v_name", name,
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(result.contains("存在重复的数据字典类目名称"));
    }

    @Test
    @DisplayName("测试字典分类排序")
    void testDictCategorySort() {
        String id1 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", UUID.randomUUID().toString(),
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String id2 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", UUID.randomUUID().toString(),
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String id3 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", UUID.randomUUID().toString(),
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .queryParam("prevId", id1)
            .queryParam("nextId", id2)
            .when()
            .get("/api%s/dict/sort/%s".formatted(base, id3))
            .then()
            .statusCode(200)
            .extract()
            .asString();

    }

    @Test
    @DisplayName("测试更新字典分类ID")
    void testCategoryIdUpdate() {
        String id = UUID.randomUUID().toString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", id,
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/create".formatted(base))
            .then()
            .statusCode(200)
            .body(is(id));

        String newID = UUID.randomUUID().toString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "id", newID,
                "v_name", UUID.randomUUID().toString(),
                "v_remark", "备注"
            ))
            .when()
            .post("/api%s/dict/update/%s".formatted(base, id))
            .then()
            .statusCode(200);

        given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/view/%s".formatted(base, id))
            .then()
            .statusCode(204);

        given()
            .header("userID", config.superUserId())
            .get("/api%s/dict/view/%s".formatted(base, newID))
            .then()
            .statusCode(200);
    }

}
