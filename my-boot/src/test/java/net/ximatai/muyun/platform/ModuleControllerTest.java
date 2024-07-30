package net.ximatai.muyun.platform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ximatai.muyun.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class ModuleControllerTest {

    @Test
    void create() {
        given()
            .contentType("application/json")
            .body(Map.of("id", "1", "name", "test1"))
//            .when()
            .post("/module/create")
            .then()
            .statusCode(200)
            .body(is("1"));
    }

}
