package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryGroup;
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
class TestGroupQuery {

    private String path = "/test_group_query";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestGroupQueryController testController;

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
    @DisplayName("测试id等于 or name等于")
    void testEqual() {
        Map<String, String> request = Map.of("id", "1", "name", "test3");

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

}

@Path("/test_group_query")
class TestGroupQueryController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility {

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
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("av_name").setType(ColumnType.VARCHAR_ARRAY))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("t_create").setTime(true).setSymbolType(QueryItem.SymbolType.RANGE)
        );
    }

    @Override
    public QueryGroup queryGroup() {
        return QueryItem.of("id").toGroup()
            .or(QueryItem.of("name").setSymbolType(QueryItem.SymbolType.EQUAL).toGroup());

    }
}
