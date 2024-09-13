package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.ITreeAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.database.metadata.DBTable;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import net.ximatai.muyun.util.TreeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestTreeAbility {

    private String path = "/TestTreeAbility";

    @Inject
    IDatabaseAccess databaseAccess;

    @Inject
    TestTreeAbilityController testController;

    String aID, bID, cID, aaID, abID, baID, aa1ID;

    @BeforeEach
    void setUp() {
        DBTable dbTable = databaseAccess.getDBInfo().getSchema("test").getTable(testController.getMainTable());
        assertEquals(dbTable.getColumn("pid").getDefaultValue(), TreeBuilder.ROOT_PID);

        databaseAccess.execute("TRUNCATE TABLE test.%s".formatted(testController.getMainTable()));

        aID = testController.create(Map.of("v_name", "A"));
        bID = testController.create(Map.of("v_name", "B"));
        cID = testController.create(Map.of("v_name", "C"));
        aaID = testController.create(Map.of("pid", aID, "v_name", "A.a"));
        abID = testController.create(Map.of("pid", aID, "v_name", "A.b"));
        baID = testController.create(Map.of("pid", bID, "v_name", "B.a"));
        aa1ID = testController.create(Map.of("pid", aaID, "v_name", "A.a.1"));
    }

    @Test
    void testTree() {
        List<TreeNode> response = given()
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(response.size(), 2);
    }

    @Test
    void testTreeA() {
        List<TreeNode> response = given()
                .queryParam("rootID", aID)
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getChildren().size(), 2);
    }

    @Test
    void testTreeANotShowMe() {
        List<TreeNode> response = given()
                .queryParam("rootID", aID)
                .queryParam("showMe", false)
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(2, response.size());
        assertEquals("A.a", response.getFirst().getLabel());
        assertEquals("A.b", response.getLast().getLabel());

        //修改顺序
        testController.update(response.getFirst().getId(), Map.of("n_sort", 2));
        testController.update(response.getLast().getId(), Map.of("n_sort", 1));

        List<TreeNode> response2 = given()
                .queryParam("rootID", aID)
                .queryParam("showMe", false)
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(2, response2.size());
        assertEquals("A.b", response2.getFirst().getLabel());
        assertEquals("A.a", response2.getLast().getLabel());
    }

    @Test
    void testTreeSort() {
        String sortRes = given()
                .queryParam("prevId", aID)
                .queryParam("nextId", bID)
                .get("%s/update/%s/sort".formatted(path, cID))
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals("A", response.getFirst().getLabel());
        assertEquals("B", response.getLast().getLabel());
    }

    @Test
    void testTreeSorCrossParent() {
        String sortRes = given()
                .queryParam("prevId", aID)
                .queryParam("nextId", bID)
                .queryParam("parentId", "") // 说明移动到根节点
                .get("%s/update/%s/sort".formatted(path, aa1ID))
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(aa1ID, response.get(1).getId());
    }

    @Test
    void testTreeSorCrossParent2() {
        String sortRes = given()
                .queryParam("prevId", aaID)
                .queryParam("nextId", abID)
                .queryParam("parentId", aID) // 说明移动到根节点
                .get("%s/update/%s/sort".formatted(path, cID))
                .then()
                .statusCode(200)
                .extract()
                .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
                .get("%s/tree".formatted(path))
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {

                });

        assertEquals(2, response.size());
        assertEquals(aID, response.get(0).getId());
        assertEquals(bID, response.get(1).getId());

        assertEquals(3, response.get(0).getChildren().size());
        assertEquals(cID, response.get(0).getChildren().get(1).getId());
    }

}

@Path("/TestTreeAbility")
class TestTreeAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, ITreeAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testtreeability";
    }

    @Override
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
                .setSchema(getSchemaName())
                .setPrimaryKey(Column.ID_POSTGRES)
                .addColumn(Column.of("v_name").setType("varchar"))
                .addColumn(Column.of("pid").setType("varchar"))
                .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

}
