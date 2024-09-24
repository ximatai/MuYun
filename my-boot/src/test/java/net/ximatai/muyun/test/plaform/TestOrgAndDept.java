package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestOrgAndDept {

    String base = PlatformConst.BASE_PATH;

    @Test
    void test() {
        String org_id = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "机构"
            ))
            .when()
            .post("%s/organization/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String dept_id = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "部门",
                "id_at_org_organization", org_id
            ))
            .when()
            .post("%s/department/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(org_id);
        assertNotNull(dept_id);

        Map row = given()
            .get("%s/department/view/%s".formatted(base, dept_id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("机构", row.get("v_name_at_org_organization"));

        String res = given()
            .contentType("application/json")
            .body(Map.of(
                "v_name", "部门2"
            ))
            .when()
            .post("%s/department/create".formatted(base))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertTrue(res.contains("部门必须归属具体机构"));
    }

}
