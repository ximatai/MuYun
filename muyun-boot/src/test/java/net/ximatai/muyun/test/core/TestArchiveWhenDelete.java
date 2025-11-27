package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IArchiveWhenDelete;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.database.core.metadata.DBTable;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestArchiveWhenDelete {

    private String path = "/TestArchiveWhenDelete";

    @Inject
    IDatabaseOperations<String> databaseOperations;

    @Inject
    TestArchiveWhenDeleteController controller;

    @Test
    @DisplayName("验证归档表存在及其包含的列")
    void testArchiveTableExists() {
        String archiveTableName = controller.getArchiveTableName();
        DBTable archiveTable = databaseOperations.getDBInfo().getSchema(controller.getSchemaName()).getTable(archiveTableName);
        assertNotNull(archiveTable);
        assertTrue(archiveTable.contains("name"));
        assertTrue(archiveTable.contains("t_create"));
        assertTrue(archiveTable.contains("t_archive"));
        assertTrue(archiveTable.contains("id_at_auth_user__archive"));
    }

    @Test
    @DisplayName("验证删除后记录被归档并可恢复")
    void testDelete() {
        Map<String, Object> request = Map.of("name", "test");

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        HashMap response = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertNotNull(response.get("name"));

        given().get("/api%s/delete/%s".formatted(path, id)).then().statusCode(200);

        given().get("/api%s/view/%s".formatted(path, id)).then().statusCode(204);

        Map row = (Map) databaseOperations.row("select * from %s.%s where id = ?".formatted(controller.getSchemaName(), controller.getArchiveTableName()), id);

        assertNotNull(row.get("t_archive"));

        PageResult<Map> response2 = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(0, response2.getTotal());

        controller.restore(id);

        response2 = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(1, response2.getTotal());

        row = (Map) databaseOperations.row("select * from %s.%s where id = ?".formatted(controller.getSchemaName(), controller.getArchiveTableName()), id);

        assertNull(row);

    }

}

@Path("/TestArchiveWhenDelete")
class TestArchiveWhenDeleteController extends Scaffold implements ICURDAbility, ITableCreateAbility, IArchiveWhenDelete {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testarchivewhendelete";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

}
