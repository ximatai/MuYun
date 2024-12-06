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
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.database.metadata.DBTable;
import net.ximatai.muyun.model.TreeNode;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import net.ximatai.muyun.util.TreeBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestTreeAbility {

    private String path = "/TestTreeAbility";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestTreeAbilityController testController;

    String aID, bID, cID, aaID, abID, baID, aa1ID;

    @BeforeEach
    void setUp() {
        DBTable dbTable = databaseOperations.getDBInfo().getSchema("test").getTable(testController.getMainTable());
        assertEquals(dbTable.getColumn("pid").getDefaultValue(), TreeBuilder.ROOT_PID);

        databaseOperations.execute("TRUNCATE TABLE test.%s".formatted(testController.getMainTable()));

        aID = testController.create(Map.of("v_name", "A"));
        bID = testController.create(Map.of("v_name", "B"));
        cID = testController.create(Map.of("v_name", "C"));
        aaID = testController.create(Map.of("pid", aID, "v_name", "A.a"));
        abID = testController.create(Map.of("pid", aID, "v_name", "A.b"));
        baID = testController.create(Map.of("pid", bID, "v_name", "B.a"));
        aa1ID = testController.create(Map.of("pid", aaID, "v_name", "A.a.1"));
    }

    @Test
    @DisplayName("测试更新节点时不能编辑节点的父节点为其子孙节点")
    void testEndlessLoop() {
        String x = testController.create(Map.of("v_name", "x"));
        String y = testController.create(Map.of("v_name", "y", "pid", x));
        String z = testController.create(Map.of("v_name", "z", "pid", y));

        MyException exception = assertThrows(MyException.class, () -> {
            testController.update(x, Map.of("pid", z));
        });

        assertEquals(exception.getMessage(), "不能编辑该节点的父节点为其子孙节点");
    }

    @Test
    @DisplayName("测试树结构API返回结点数量")
    void testTree() {
        List<TreeNode> response = given()
            .get("/api%s/tree".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(response.size(), 3);
    }

    @Test
    @DisplayName("测试将节点的父节点设置为其自身时返回的错误信息")
    void testTreeNodePIDSelf() {
        String error = given()
            .contentType("application/json")
            .body(Map.of("pid", aID))
            .post("/api%s/update/%s".formatted(path, aID))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        assertEquals(error, "树结构的父节点不能是它自身");
    }

    @Test
    @DisplayName("测试树结构API返回的根节点及其子节点数量")
    void testTreeA() {
        List<TreeNode> response = given()
            .queryParam("rootID", aID)
            .get("/api%s/tree".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(response.size(), 1);
        assertEquals(response.get(0).getChildren().size(), 2);
    }

    @Test
    @DisplayName("测试树结构API在特定参数下返回的节点及其顺序")
    void testTreeANotShowMe() {
        List<TreeNode> response = given()
            .queryParam("rootID", aID)
            .queryParam("showMe", false)
            .get("/api%s/tree".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(2, response.size());
        assertEquals("A.a", response.getFirst().getLabel());
        assertEquals("A.b", response.getLast().getLabel());

        //修改顺序
        testController.update(response.getFirst().getId(), Map.of("n_order", 2));
        testController.update(response.getLast().getId(), Map.of("n_order", 1));

        List<TreeNode> response2 = given()
            .queryParam("rootID", aID)
            .queryParam("showMe", false)
            .get("/api%s/tree".formatted(path))
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
    @DisplayName("测试树结构API的排序功能及其对树结构的影响")
    void testTreeSort() {
        String sortRes = given()
            .queryParam("prevId", aID)
            .queryParam("nextId", bID)
            .get("/api%s/sort/%s".formatted(path, cID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
            .get("/api%s/tree".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals("A", response.getFirst().getLabel());
        assertEquals("B", response.getLast().getLabel());
    }

    @Test
    @DisplayName("测试树结构API的跨父节点排序功能及其对树结构的影响")
    void testTreeSorCrossParent() {
        String sortRes = given()
            .queryParam("prevId", aID)
            .queryParam("nextId", bID)
            .queryParam("parentId", "") // 说明移动到根节点
            .get("/api%s/sort/%s".formatted(path, aa1ID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
            .get("/api%s/tree".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertEquals(aa1ID, response.get(1).getId());
    }

    @Test
    @DisplayName("测试树结构API的跨父节点排序功能及其对树结构的影响（移动到指定父节点）")
    void testTreeSorCrossParent2() {
        String sortRes = given()
            .queryParam("prevId", aaID)
            .queryParam("nextId", abID)
            .queryParam("parentId", aID) // 说明移动到根节点
            .get("/api%s/sort/%s".formatted(path, cID))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", sortRes);

        List<TreeNode> response = given()
            .get("/api%s/tree".formatted(path))
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
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("pid"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

}
