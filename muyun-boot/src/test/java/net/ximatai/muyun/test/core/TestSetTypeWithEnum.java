package net.ximatai.muyun.test.core;

import io.quarkus.runtime.Startup;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestSetTypeWithEnum {
    @Inject
    TestSetTypeWithEnumController testSetTypeWithEnumController;

    @Test
    void testCreat() {
        String id = testSetTypeWithEnumController.create(Map.of(
            "name", "小红",
            "age", 23,
            "creat_time", "1988-01-01",
            "update_time", "2001-01-01"
        ));

        Map<String, ?> map = testSetTypeWithEnumController.view(id);

        assertEquals("小红", map.get("name").toString());
        assertEquals(23, map.get("age"));
        assertEquals("1988-01-01", map.get("creat_time").toString());
        assertEquals("2001-01-01 00:00:00.0", map.get("update_time").toString());

        String string = given()
            .get("/api/TestSetTypeWithEnumController/view/" + id)
            .then()
            .extract()
            .asString();

        JsonObject entries = new JsonObject(string);
        String dataInJson = entries.getString("name");
        String dataInJson2 = entries.getString("age");
        String dataInJson3 = entries.getString("creat_time");
        String dataInJson4 = entries.getString("update_time");

        assertEquals("小红", dataInJson);
        assertEquals("23", dataInJson2);
        assertEquals("1988-01-01", dataInJson3);
        assertEquals("2001-01-01 00:00:00", dataInJson4);

    }
}

@Startup
@Path("/TestSetTypeWithEnumController")
class TestSetTypeWithEnumController extends Scaffold implements ICURDAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("age").setType(ColumnType.INT))
            .addColumn(Column.of("creat_time").setType(ColumnType.DATE))
            .addColumn(Column.of("update_time").setType(ColumnType.TIMESTAMP));  // 字段名
    }
}
