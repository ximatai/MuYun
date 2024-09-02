package net.ximatai.muyun.platform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.platform.controller.TestController;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
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
class TestControllerTest {

    @Inject
    IDatabaseAccess databaseAccess;

    @Inject
    TestController testController;

    String tableName;

    List<String> ids;

    @BeforeEach
    @Transactional
    void setUp() {
        tableName = testController.getMainTable();
        databaseAccess.execute("TRUNCATE TABLE %s".formatted(tableName));

        var id1 = testController.create(Map.of("id", "1", "name", "test1"));
        var id2 = testController.create(Map.of("id", "2", "name", "test2"));
        var id3 = testController.create(Map.of("id", "3", "name", "test3"));

        ids = List.of(id1, id2, id3);

//        var id1 = databaseAccess.insert("insert into test_table (name) values (:name) ", Map.of("name", "test1"));
//        var id2 = databaseAccess.insert("insert into test_table (name) values (:name) ", Map.of("name", "test2"));
//        var id3 = databaseAccess.insert("insert into test_table (name) values (:name) ", Map.of("name", "test3"));

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

        Map e = (Map) databaseAccess.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertEquals(request.get("id"), e.get("id"));
        assertEquals(request.get("name"), e.get("name"));
        assertNull(e.get("name2"));
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

        Map e = (Map) databaseAccess.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

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
            .statusCode(500);
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

        Map e = (Map) databaseAccess.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

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

        assertThrows(MyDatabaseException.class, () -> {
            databaseAccess.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));
        });
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
