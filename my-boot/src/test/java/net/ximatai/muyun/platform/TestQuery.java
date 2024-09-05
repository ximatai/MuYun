package net.ximatai.muyun.platform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestQuery {

    private String path = "/test_query";

    @Inject
    IDatabaseAccess databaseAccess;

    @Inject
    TestQueryController testController;

    String tableName;

    List<String> ids;

    @BeforeEach
    void setUp() {
        tableName = testController.getMainTable();
        databaseAccess.execute("TRUNCATE TABLE %s".formatted(tableName));

        var id1 = testController.create(Map.of("id", "1", "name", "test1", "t_create", "2024-01-01 12:00:00"));
        var id2 = testController.create(Map.of("id", "2", "name", "test2", "t_create", "2024-01-02 12:00:00"));
        var id3 = testController.create(Map.of("id", "3", "name", "test3", "t_create", "2024-01-03 12:00:00"));
        var id4 = testController.create(Map.of("id", "4", "name", "test4", "t_create", "2024-01-04 12:00:00"));
        var id5 = testController.create(Map.of("id", "5", "name", "test5", "t_create", "2024-01-05 12:00:00"));
        var id6 = testController.create(Map.of("id", "6", "name", "test6", "t_create", "2024-01-06 12:00:00"));
        var id7 = testController.create(Map.of("id", "7", "name", "test7", "t_create", "2024-01-07 12:00:00"));
        var id8 = testController.create(Map.of("id", "8", "name", "test8", "t_create", "2024-01-08 12:00:00"));

        ids = List.of(id1, id2, id3, id4, id5, id6, id7, id8);
    }

    @Test
    void testEqual() {
        String id = ids.getFirst();

        Map<String, String> request = Map.of("id", id);

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response.getTotal());
        assertEquals(1, response.getList().size());
        assertEquals(id, response.getList().get(0).get("id"));
    }

    @Test
    void testNotEqual() {
        String id = ids.getFirst();

        Map<String, String> request = Map.of("no_id", id);

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(7, response.getTotal());
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
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("id").setAlias("no_id").setSymbolType(QueryItem.SymbolType.NOT_EQUAL),
            QueryItem.of("name")
        );
    }
}
