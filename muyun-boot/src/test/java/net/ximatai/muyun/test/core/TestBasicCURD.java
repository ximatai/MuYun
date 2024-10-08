package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestBasicCURD {

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestBasicCURDController testController;

    String tableName;

    List<String> ids;

    @BeforeEach
    void setUp() {
        tableName = testController.getMainTable();
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(tableName));

        var id1 = testController.create(Map.of("id", "1", "name", "test1"));
        var id2 = testController.create(Map.of("id", "2", "name", "test2"));
        var id3 = testController.create(Map.of("id", "3", "name", "test3"));

        ids = List.of(id1, id2, id3);

//        var id1 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test1"));
//        var id2 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test2"));
//        var id3 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test3"));

    }

    @Test
    void testPageView() {
        String id = ids.getFirst();
        PageResult response = given()
            .queryParam("page", 1)
            .queryParam("size", 2)
            .get("/test/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.getTotal(), 3);
        assertEquals(response.getList().size(), 2);
        assertEquals(response.getPage(), 1);
        assertEquals(response.getSize(), 2);
    }

    @Test
    void testPageViewSort() {
        PageResult<HashMap> response = given()
            .queryParam("page", 1)
            .queryParam("size", 2)
            .queryParam("sort", "t_create")
            .get("/test/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.getList().getFirst().get("id"), "1");
    }

    @Test
    void testPageViewSortDesc() {
        PageResult<HashMap> response = given()
            .queryParam("page", 1)
            .queryParam("size", 2)
            .queryParam("sort", "t_create,desc")
            .queryParam("sort", "name,desc")
            .get("/test/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals("3", response.getList().getFirst().get("id"));
    }

    @Test
    void testCreate() {
        String id = "666";
        Map<String, String> request = Map.of("id", id, "name", "test", "name2", "test2");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/create")
            .then()
            .statusCode(200)
            .body(is(id));

        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertEquals(request.get("id"), e.get("id"));
        assertEquals(request.get("name"), e.get("name"));
        assertNull(e.get("name2"));
        assertNull(e.get("t_update"));
        assertNotNull(e.get("t_create"));
    }

    @Test
    void testUpdate() {
        String id = ids.getFirst();
        Map<String, String> request = Map.of("name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(200)
            .body(is("1"));

        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertNotNull(e.get("t_update"));
        assertEquals(request.get("name"), e.get("name"));
    }

    @Test
    void testUpdateNotFound() {
        String id = "666";
        Map<String, String> request = Map.of("name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(404);
    }

    @Test
    void testUpdateFieldNotExists() {
        String id = ids.getFirst();
        Map<String, String> request = Map.of("unknown", "field");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(200);
    }

    @Test
    void testGet() {
        String id = ids.getFirst();
        HashMap response = given()
            .get("/test/view/" + id)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertEquals(e.get("name"), response.get("name"));
        assertEquals(e.get("id"), response.get("id"));
    }

    @Test
    void testGetNotFound() {
        String id = "666";
        given()
            .get("/test/view/" + id)
            .then()
            .statusCode(404);
    }

    @Test
    void testDelete() {
        String id = ids.getFirst();
        given()
            .get("/test/delete/" + id)
            .then()
            .statusCode(200);

        assertNull(
            databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id))
        );
    }

    @Test
    void testDeleteNotFound() {
        String id = "666";
        given()
            .get("/test/delete/" + id)
            .then()
            .statusCode(404);
    }

}

@Path("/test")
class TestBasicCURDController extends Scaffold implements ICURDAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))
            .addColumn(Column.of("t_create"))
            .addColumn(Column.of("t_update"));

    }
}
