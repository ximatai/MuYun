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
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
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

    String aID;

    @BeforeEach
    void setUp() {
        databaseAccess.execute("TRUNCATE TABLE test.%s".formatted(testController.getMainTable()));

        aID = testController.create(Map.of("v_name", "A"));
        var bID = testController.create(Map.of("v_name", "B"));
        var aaID = testController.create(Map.of("pid", aID, "v_name", "A.a"));
        testController.create(Map.of("pid", aID, "v_name", "A.b"));
        var baID = testController.create(Map.of("pid", bID, "v_name", "B.a"));

        var aa1ID = testController.create(Map.of("pid", aaID, "v_name", "A.a.1"));
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
        testController.update(response.getFirst().getId(), Map.of("i_sort", 2));
        testController.update(response.getLast().getId(), Map.of("i_sort", 1));

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
