package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ISortAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestSortAbility {

    private String path = "/TestSortAbility";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestSortAbilityController testController;

    String aID;

    @BeforeEach
    void setUp() {
        databaseOperations.execute("TRUNCATE TABLE test.%s".formatted(testController.getMainTable()));
    }

    @Test
    @DisplayName("测试排序功能")
    void testSort() {

        var idA = testController.create(Map.of("v_name", "A"));
        var idB = testController.create(Map.of("v_name", "B"));
        var idC = testController.create(Map.of("v_name", "C"));
        var idD = testController.create(Map.of("v_name", "D"));

        PageResult<Map> response = given()
            .queryParam("noPage", true)
            .get("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(4, response.getSize());
        assertEquals(3, response.getList().get(2).get("n_order"));

        //把C移动到AB之间
        String sortRes = given()
            .queryParam("prevId", idA)
            .queryParam("nextId", idB)
            .get("/api%s/sort/%s".formatted(path, idC))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", sortRes);

        Map<String, ?> c = testController.view(idC);
        Map<String, ?> b = testController.view(idB);

        assertEquals(new BigDecimal(2), c.get("n_order"));
        assertEquals(new BigDecimal(3), b.get("n_order"));

        given()
            .queryParam("prevId", idD)
//            .queryParam("nextId", idB)
            .get("/api%s/sort/%s".formatted(path, idA))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals(new BigDecimal(3), testController.view(idD).get("n_order"));
        assertEquals(new BigDecimal(4), testController.view(idA).get("n_order"));
    }

}

@Path("/TestSortAbility")
class TestSortAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, ISortAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testsortability";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

}
