package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestSoftDelete {

    private String path = "/TestSoftDelete";

    @Inject
    IDatabaseAccess databaseAccess;

    @Test
    void testDelete() {
        Map<String, Object> request = Map.of("name", "test");

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertNotNull(response.get("name"));

        given().get("%s/delete/%s".formatted(path, id)).then().statusCode(200);

        given().get("/test/view/" + id).then().statusCode(404);

        Map row = (Map) databaseAccess.row("select * from testsoftdelete where id = ?", id);

        assertEquals(true, row.get("b_delete"));

        PageResult<Map> response2 = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(0, response2.getTotal());
    }

}

@Path("/TestSoftDelete")
class TestSoftDeleteController extends Scaffold implements ICURDAbility, ITableCreateAbility, ISoftDeleteAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testsoftdelete";
    }

    @Override
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

}
