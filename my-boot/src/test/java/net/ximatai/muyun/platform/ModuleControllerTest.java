package net.ximatai.muyun.platform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasKey;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class ModuleControllerTest {

    String id = UUID.randomUUID().toString();

    @Test
    void CURD() {
        given()
            .contentType("application/json")
            .body(Map.of("id", id, "name", "test1"))
//            .when()
            .post("/module/create")
            .then()
            .statusCode(200)
            .body(is(id));


        HashMap response = given()
            .get("/module/view/" + id)
            .then()
            .statusCode(200)
            .body("$", hasKey("name"))
            .extract()
            .as(HashMap.class);


        assertEquals("test1", response.get("name"));

        given()
            .contentType("application/json")
            .body(Map.of("name", "test2"))
//            .when()
            .post("/module/update/" + id)
            .then()
            .statusCode(200)
            .body(is("1"));


        given()
            .get("/module/delete/" + id)
            .then()
            .statusCode(200)
            .body(is("1"));


        given()
            .get("/module/view/" + id)
            .then()
            .statusCode(500);


        given()
            .contentType("application/json")
            .body(Map.of("name", "test3"))
            .post("/module/update/" + id)
            .then()
            .statusCode(500);

        given()
            .get("/module/delete/" + id)
            .then()
            .statusCode(500);
    }


}
