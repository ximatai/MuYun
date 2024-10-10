package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.MuYunConfig;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.platform.PlatformConst;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestOrgAndDept {

    @Inject
    MuYunConfig config;

    String base = PlatformConst.BASE_PATH;

    @Test
    void test() {
        String orgId = given()
            .contentType("application/json")
            .header("userID", config.superUserId())
            .body(Map.of(
                "v_name", "机构",
                "dict_org_type", "jituan"
            ))
            .when()
            .post("%s/organization/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map orgRow = given()
            .header("userID", config.superUserId())
            .get("%s/organization/view/%s".formatted(base, orgId))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("集团公司", orgRow.get("v_name_at_dict_org_type"));

        List<TreeNode> dictTree = given()
            .header("userID", config.superUserId())
            .get("%s/dict/tree/%s".formatted(base, "org_type"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(2, dictTree.size());

        String deptId = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "部门",
                "id_at_org_organization", orgId
            ))
            .when()
            .post("%s/department/create".formatted(base))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(orgId);
        assertNotNull(deptId);

        Map row = given()
            .header("userID", config.superUserId())
            .get("%s/department/view/%s".formatted(base, deptId))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("机构", row.get("v_name_at_org_organization"));

        String res = given()
            .header("userID", config.superUserId())
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
