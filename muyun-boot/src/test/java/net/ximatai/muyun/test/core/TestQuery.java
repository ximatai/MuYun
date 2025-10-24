package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestQuery {

    private String path = "/test_query";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestQueryController testController;

    String tableName;

    @BeforeEach
    void setUp() {
        tableName = testController.getMainTable();
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(tableName));

        testController.create(Map.of("id", "1", "name", "test1", "av_name", List.of("a"), "t_create", "2024-01-01 12:00:00"));
        testController.create(Map.of("id", "2", "name", "test2", "av_name", List.of("a", "b"), "t_create", "2024-01-02 12:00:00"));
        testController.create(Map.of("id", "3", "name", "test3", "av_name", List.of("a", "b", "c"), "t_create", "2024-01-03 12:00:00"));
        testController.create(Map.of("id", "4", "name", "test4", "av_name", List.of("a", "b", "c", "d"), "t_create", "2024-01-04 12:00:00"));
        testController.create(Map.of("id", "5", "name", "test5", "av_name", List.of("a", "b", "c"), "t_create", "2024-01-05 12:00:00"));
        testController.create(Map.of("id", "6", "name", "test6", "av_name", List.of("b", "c"), "t_create", "2024-01-06 12:00:00"));
        testController.create(Map.of("id", "7", "name", "test7", "av_name", List.of("a", "c"), "t_create", "2024-01-07 12:00:00"));
        testController.create(Map.of("id", "8", "name", "test8", "av_name", List.of("c"), "t_create", "2024-01-08 12:00:00"));
    }

    @Test
    @DisplayName("测试等于条件")
    void testEqual() {
        Map<String, String> request = Map.of("id", "1");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response.getTotal());
        assertEquals(1, response.getList().size());
        assertEquals("1", response.getList().get(0).get("id"));
    }

    @Test
    @DisplayName("测试等于空字符串条件")
    void testEqualWithBlankString() {
        Map<String, String> request = Map.of("id", "");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(8, response.getTotal());
    }

    @Test
    @DisplayName("测试不等于条件")
    void testNotEqual() {
        Map<String, String> request = Map.of("no_id", "1");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(7, response.getTotal());
    }

    @Test
    @DisplayName("测试IN条件")
    void testIn() {
        Map<String, ?> request = Map.of("in_id", List.of("1", "2", "3"));

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(3, response.getTotal());
    }

    @Test
    @DisplayName("测试NOT IN条件")
    void testNotIn() {
        Map<String, ?> request = Map.of("not_in_id", List.of("1", "2", "3"));

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(5, response.getTotal());
    }

    @Test
    @DisplayName("测试LIKE条件")
    void testLike() {
        Map<String, ?> request = Map.of("name", "test");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(8, response.getTotal());
    }

    @Test
    @DisplayName("测试查询范围从2024-01-05 12:00:00到正无穷的数据")
    void testRange1() {
        Map<String, ?> request = Map.of("t_create", new String[]{"2024-01-05 12:00:00", null});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(4, response.getTotal());
    }

    @Test
    @DisplayName("测试查询范围从负无穷到2024-01-05 12:00:00的数据")
    void testRange2() {
        Map<String, ?> request = Map.of("t_create", new String[]{null, "2024-01-05 12:00:00"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(5, response.getTotal());
    }

    @Test
    @DisplayName("测试查询范围从2024-01-05 00:00:00到2024-01-05 24:00:00的数据")
    void testRange3() {
        Map<String, ?> request = Map.of("t_create", new String[]{"2024-01-05 00:00:00", "2024-01-05 24:00:00"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response.getTotal());
    }

    @Test
    @DisplayName("测试查询范围从2024-01-01 00:00:00到2024-01-10 24:00:00的数据")
    void testRange4() {
        Map<String, ?> request = Map.of("t_create", new String[]{"2024-01-01 00:00:00", "2024-01-10 24:00:00"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(8, response.getTotal());
    }

    @Test
    @DisplayName("测试查询范围从2024-01-05 12:00:00到2024-01-05 12:00:00的数据")
    void testRange5() {
        Map<String, ?> request = Map.of("t_create", new String[]{"2024-01-05 12:00:00", "2024-01-05 12:00:00"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response.getTotal());
    }

    @Test
    @DisplayName("比较两个数组完全相同")
    void testPgArrayEqual() {
        Map<String, ?> request = Map.of("av_name1", new String[]{"a", "b"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response.getTotal());
    }

    @Test
    @DisplayName("比较两个数组有交集")
    void testPgArrayOverlap() {
        Map<String, ?> request = Map.of("av_name2", new String[]{"a", "b"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(7, response.getTotal());
    }

    @Test
    @DisplayName("获取在能被传入数组所包含的数据")
    void testPgArrayContain() {
        Map<String, ?> request = Map.of("av_name3", new String[]{"a", "b"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(2, response.getTotal());
    }

    @Test
    @DisplayName("获取包含了传入数组的数据")
    void testPgArrayBeContain() {
        Map<String, ?> request = Map.of("av_name4", new String[]{"a", "b"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(4, response.getTotal());
    }

}

@Path("/test_query")
class TestQueryController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_query";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("av_name").setType(ColumnType.VARCHAR_ARRAY))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("id").setAlias("no_id").setSymbolType(QueryItem.SymbolType.NOT_EQUAL),
            QueryItem.of("id").setAlias("in_id").setSymbolType(QueryItem.SymbolType.IN),
            QueryItem.of("id").setAlias("not_in_id").setSymbolType(QueryItem.SymbolType.NOT_IN),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("av_name").setSymbolType(QueryItem.SymbolType.PG_ARRAY_EQUAL).setAlias("av_name1"),
            QueryItem.of("av_name").setSymbolType(QueryItem.SymbolType.PG_ARRAY_OVERLAP).setAlias("av_name2"),
            QueryItem.of("av_name").setSymbolType(QueryItem.SymbolType.PG_ARRAY_CONTAIN).setAlias("av_name3"),
            QueryItem.of("av_name").setSymbolType(QueryItem.SymbolType.PG_ARRAY_BE_CONTAIN).setAlias("av_name4"),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("t_create").setTime(true).setSymbolType(QueryItem.SymbolType.RANGE)
        );
    }
}
