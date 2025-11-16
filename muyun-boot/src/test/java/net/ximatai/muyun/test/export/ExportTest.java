package net.ximatai.muyun.test.export;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IExportAbility;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.ExportColumn;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class ExportTest {
    @Inject
    BarController barController;

    @BeforeAll
    void setup() {
        // 创建 1k 的数组
        List<Map> batch = new java.util.ArrayList<>(1_000);
        for (int i = 0; i < 1_000; i++) {
            batch.add(Map.of(
                "v_name", "name_%d".formatted(i),
                "v_password", "password_%d".formatted(i),
                "i_num", i,
                "v_value", "value_%d".formatted(i)
            ));
        }
        barController.batchCreate(batch);
    }

    @Test
    @DisplayName("测试导出 JSON")
    void testExportJson() throws IOException {
        InputStream inputStream = given()
            .contentType("application/json")
            .body(Map.of())
            .when()
            .post("/api/bar/export/json")
            .then()
            .statusCode(200)
            .extract()
            .asInputStream();

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        List<Map> items = mapper.readValue(inputStream, List.class);

        assertEquals(500, items.size());
        for (Map item : items) {
            assertEquals(5, item.size());
            assertTrue(item.containsKey("id"));
            assertTrue(item.containsKey("v_name"));
            assertTrue(item.containsKey("i_num"));
            assertTrue(item.containsKey("v_value"));
            assertTrue(item.containsKey("filtered"));
            Integer iNum = (Integer) item.get("i_num");
            assertTrue(iNum % 2 != 0);
            assertEquals(true, item.get("filtered"));
        }
    }
}

@Path("/bar")
class BarController extends BaseScaffold implements IExportAbility {
    @Override
    public List<QueryItem> queryItemList() {
        return List.of();
    }

    @Override
    public String getMainTable() {
        return "bar_table";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn("v_name")
            .addColumn("v_password")
            .addColumn("i_num")
            .addColumn("v_value");
    }

    @Override
    public List<ExportColumn> getExportColumns(String type) {
        return List.of(
            ExportColumn.of("id", "ID"),
            ExportColumn.of("v_name", "名称"),
            ExportColumn.of("i_num", "序号"),
            ExportColumn.of("v_value", "值"),
            ExportColumn.of("filtered", "被过滤")
        );
    }

    @Override
    public void filterExportData(List<Map> items) {
        items.removeIf((item) -> (Integer) item.get("i_num") % 2 == 0);
        items.forEach((item) -> item.put("filtered", true));
    }
}
