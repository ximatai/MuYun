package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ICommonBusinessAbility;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestCommonBusiness {

    private String path = "/TestCommonBusiness";

    @Inject
    IDatabaseOperations databaseOperations;

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

        assertTrue(response.containsKey("t_create"));
        assertTrue(response.containsKey("t_update"));
        assertTrue(response.containsKey("t_delete"));

    }

}

@Path("/TestCommonBusiness")
class TestCommonBusinessCtrl extends Scaffold implements ICURDAbility, ITableCreateAbility, ICommonBusinessAbility, ISoftDeleteAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testcommonbusiness";
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
