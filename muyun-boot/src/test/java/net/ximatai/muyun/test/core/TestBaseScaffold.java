package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestBaseScaffold {

    @Test
    void testCreateAndUpdateHasTimeColumn() {
        String id = given()
            .contentType("application/json")
            .body(Map.of("v_name", "test"))
            .when()
            .post("/api/TestBaseScaffold/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row = given()
            .get("/api/TestBaseScaffold/view/%s".formatted(id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertNotNull(row.get("t_create"));

        String createString = "1988-10-12 13:58:06";

        given()
            .contentType("application/json")
            .body(Map.of("v_name", "test", "t_create", createString))
            .when()
            .post("/api/TestBaseScaffold/update/%s".formatted(id))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row2 = given()
            .get("/api/TestBaseScaffold/view/%s".formatted(id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(createString, row2.get("t_create"));
    }

}

@Path("/TestBaseScaffold")
class TestBaseScaffoldController extends BaseScaffold {

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setInherit(BaseBusinessTable.TABLE)
            .addColumn("v_name")
            .addColumn("v_remark");
    }

    @Override
    public String getMainTable() {
        return "testbasescaffold";
    }
}
