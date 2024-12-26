package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.ability.IReferenceAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestReference {

    private String path = "/test_reference";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestReferenceController testController;

    @Inject
    TestReferableController testReferableController;

    @Inject
    TestReferableController2 testReferableController2;

    @BeforeEach
    void setUp() {
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(testController.getMainTable()));

        String idLevel3 = testReferableController2.create(Map.of("v_name", "test_level3"));

        String id1 = testReferableController.create(Map.of("v_name", "test_level2", "id_at_test_table_referable2", idLevel3));
        String id2 = testReferableController.create(Map.of("v_name", "test_level2"));

        testController.create(Map.of("name", "test1", "t_create", "2024-01-01 12:00:00", "id_at_test_table_referable", id1));
        testController.create(Map.of("name", "test2", "t_create", "2024-01-02 12:00:00", "id_at_test_table_referable", id2));
        testController.create(Map.of("name", "test3", "t_create", "2024-01-02 12:00:00"));

    }

    @Test
    @DisplayName("测试查询引用接口的结果是否符合预期")
    void testEqual() {
        Map<String, String> request = Map.of("id", "1");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("/api%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(3, response.getTotal());

        response.getList().forEach(item -> {
            switch (item.get("name").toString()) {
                case "test1":
                    assertEquals("test_level2", item.get("v_name_at_test_table_referable"));
                    assertEquals("test_level3", item.get("haha"));
                    break;
                case "test2":
                    assertEquals("test_level2", item.get("v_name_at_test_table_referable"));
                    break;
                case "test3":
                    assertNull(item.get("v_name_at_test_table_referable"));
            }
        });

    }
}

@Path("/test_reference")
class TestReferenceController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IReferenceAbility {

    @Inject
    TestReferableController testReferableController;

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_reference";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("id_at_test_table_referable").setType(ColumnType.VARCHAR))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE)
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            testReferableController.toReferenceInfo("id_at_test_table_referable")
                .setDeep()
                .add("haha", "haha")
        );
    }
}

@Path("/test_referable")
class TestReferableController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IReferableAbility, IReferenceAbility {

    @Inject
    TestReferableController2 testReferableController2;

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_referable";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name"))
            .addColumn("id_at_test_table_referable2")
            .addColumn(Column.of("t_create"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id")
        );
    }

    @Override
    public List<ReferenceInfo> getReferenceList() {
        return List.of(
            testReferableController2.toReferenceInfo("id_at_test_table_referable2").add("v_name", "haha")
        );
    }
}

@Path("/test_referable2")
class TestReferableController2 extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IReferableAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_referable2";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("t_create"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id")
        );
    }
}


