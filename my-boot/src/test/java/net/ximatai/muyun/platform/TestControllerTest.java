package net.ximatai.muyun.platform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import net.ximatai.muyun.platform.entity.TestEntity;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestControllerTest {

    @Inject
    EntityManager entityManager;

    private static List<TestEntity> data;

    @BeforeEach
    @Transactional
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE test_table").executeUpdate();

        TestEntity e1 = new TestEntity();
        e1.name = "test1";
        TestEntity e2 = new TestEntity();
        e2.name = "test2";
        TestEntity e3 = new TestEntity();
        e3.name = "test3";

        data = List.of(e1, e2, e3);
        data.forEach(entityManager::persist);
    }

    @Test
    void testCreate() {
        String id = "666";
        Map<String, String> request = Map.of("id", id, "name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/create")
            .then()
            .statusCode(200)
            .body(is(id));

        TestEntity e = entityManager.find(TestEntity.class, id);

        assertEquals(request.get("id"), e.id);
        assertEquals(request.get("name"), e.name);
    }

    @Test
    void testUpdate() {
        String id = data.getFirst().id;
        Map<String, String> request = Map.of("name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(200)
            .body(is("1"));

        TestEntity e = entityManager.find(TestEntity.class, id);

        assertEquals(request.get("name"), e.name);
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
        String id = data.getFirst().id;
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
        String id = data.getFirst().id;
        HashMap response = given()
            .get("/test/view/" + id)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(data.getFirst().name, response.get("name"));
        assertEquals(data.getFirst().id, response.get("id"));
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
        String id = data.getFirst().id;
        given()
            .get("/test/delete/" + id)
            .then()
            .statusCode(200);

        TestEntity e = entityManager.find(TestEntity.class, id);
        assertNull(e);
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
