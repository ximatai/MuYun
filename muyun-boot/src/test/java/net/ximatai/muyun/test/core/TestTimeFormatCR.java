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
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestTimeFormatCR {

    @Inject
    TestTimeFormatCRController testController;

    @Test
    @DisplayName("测试时间格式创建和查看")
    void testCreate() {
        String id = testController.create(Map.of(
            "d_test", "1988-01-01",
            "t_test2", "2001-01-01"
        ));

        Map<String, ?> map = testController.view(id);

        assertEquals("1988-01-01", map.get("d_test").toString());
        assertEquals("2001-01-01 00:00:00.0", map.get("t_test2").toString());

        String string = given()
            .get("/api/TestTimeFormatCRController/view/" + id)
            .then()
            .extract()
            .asString();

        JsonObject entries = new JsonObject(string);
        String dataInJson = entries.getString("d_test");
        String dataInJson2 = entries.getString("t_test2");

        assertEquals("1988-01-01", dataInJson);
        assertEquals("2001-01-01 00:00:00", dataInJson2);

    }

}

@Startup
@Path("/TestTimeFormatCRController")
class TestTimeFormatCRController extends Scaffold implements ICURDAbility, ITableCreateAbility {

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
            .addColumn(Column.of("d_test"))
            .addColumn(Column.of("t_test2"));

    }
}
