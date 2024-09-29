package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestModuleAndAction {
    String base = PlatformConst.BASE_PATH;

    @Test
    void test() {
        String moduleID = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试",
                "v_alias", "test"
            ))
            .when()
            .post("%s/module/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<Map> response = given()
            .get("%s/module/view/%s/child/app_module_action".formatted(base, moduleID))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(6, response.size());

    }
}
