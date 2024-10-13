package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestWildcardPath {

    @Test
    void test() {
        String result = given()
            .get("/commondoc/wildcard/wangpan/view")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        System.out.println(result);
    }
}

@Path("/commondoc/wildcard/{type}")
class WildcardPathController {

    @GET
    @Path("view")
    public String view() {
        return getPath();
    }

    String getPath() {
        Path annotation = this.getClass().getAnnotation(Path.class);
        if (annotation != null) {
            return annotation.value();
        }
        return null;
    }
}
