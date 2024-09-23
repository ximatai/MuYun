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
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.model.ReferenceInfo;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestReference {

    private String path = "/test_reference";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestReferenceController testController;

    @Inject
    TestReferableController testReferableController;

    @BeforeEach
    void setUp() {
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(testController.getMainTable()));

        String id1 = testReferableController.create(Map.of("v_name", "test1"));
        String id2 = testReferableController.create(Map.of("v_name", "test2"));

        testController.create(Map.of("name", "test1", "t_create", "2024-01-01 12:00:00", "id_at_test_table_referable", id1));
        testController.create(Map.of("name", "test2", "t_create", "2024-01-02 12:00:00", "id_at_test_table_referable", id2));
        testController.create(Map.of("name", "test3", "t_create", "2024-01-02 12:00:00"));

    }

    @Test
    void testEqual() {
        Map<String, String> request = Map.of("id", "1");

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(3, response.getTotal());

        response.getList().forEach(item -> {
            switch (item.get("name").toString()) {
                case "test1":
                    assertEquals("test1", item.get("v_name_at_test_table_referable"));
                    break;
                case "test2":
                    assertEquals("test2", item.get("v_name_at_test_table_referable"));
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
    public TableWrapper getTableWrapper() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))
            .addColumn(Column.of("id_at_test_table_referable").setType("varchar"))
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
            new ReferenceInfo("id_at_test_table_referable", testReferableController).autoPackage()
        );
    }
}

@Path("/test_referable")
class TestReferableController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IReferableAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_referable";
    }

    @Override
    public TableWrapper getTableWrapper() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name").setType("varchar"))
            .addColumn(Column.of("t_create"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id")
        );
    }
}
