package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestComplexityCreate {

    private String path = "/test_complexity_create";

    @Test
    @DisplayName("测试创建包含字符串数组的对象")
    void testArray() {
        Map<String, Object> request = Map.of("name", "test", "names", List.of("a", "b"));

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        List list = (List) response.get("names");
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    @Test
    @DisplayName("测试创建包含整数数组的对象")
    void testIntArray() {
        Map<String, Object> request = Map.of("name", "test", "ints", List.of(1, 2, 3));

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        List list = (List) response.get("ints");
        assertTrue(list.contains(1));
        assertTrue(list.contains(2));
        assertTrue(list.contains(3));
    }

    @Test
    @DisplayName("测试创建包含JSON数组的对象")
    void testJsonArray() {
        Map<String, Object> request = Map.of("name", "test", "json_test", List.of("a", "b"));

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        List list = (List) response.get("json_test");
        assertTrue(list.contains("a"));
        assertTrue(list.contains("b"));
    }

    @Test
    @DisplayName("测试创建和更新包含JSON对象的对象")
    void testJsonObject() {
        Map<String, Object> request = Map.of("name", "test", "json_test", Map.of("x", 1));

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        Map map = (Map) response.get("json_test");
        assertEquals(1, map.get("x"));

        String update = given()
            .contentType("application/json")
            .body(Map.of("name", "test", "json_test", Map.of("x", 2)))
            .when()
            .post("/api%s/update/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();

        assertEquals("1", update);

        HashMap response2 = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        Map map2 = (Map) response2.get("json_test");
        assertEquals(2, map2.get("x"));
    }

}

@Path("/test_complexity_create")
class TestComplexityCreateController extends Scaffold implements ICURDAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_complexity_create_table";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("t_create").setDefaultValue("now()"))
            .addColumn(Column.of("names").setType(ColumnType.VARCHAR_ARRAY))
            .addColumn(Column.of("ints").setType(ColumnType.INT_ARRAY))
            .addColumn(Column.of("json_test").setType(ColumnType.JSON));

    }

}
