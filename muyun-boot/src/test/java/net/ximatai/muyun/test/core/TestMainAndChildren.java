package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IChildAbility;
import net.ximatai.muyun.ability.IChildrenAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.BatchResult;
import net.ximatai.muyun.model.ChildTableInfo;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
class TestMainAndChildren {

    private String mainPath = "/testmain";
    private String childPath = "/testchildren";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestMain testMain;

    String idMain;
    String idChild;

    String childAlias = "child";

    @BeforeEach
    void setUp() {
        idMain = testMain.create(Map.of("v_name", "main", childAlias, List.of(
            Map.of("v_name", "child1")
        )));

        List<Map> childTableList = testMain.getChildTableList(idMain, childAlias, null);

        idChild = (String) childTableList.get(0).get("id");
    }

    @Test
    @DisplayName("测试插入主表和子表记录后能否正确获取")
    void testInsertOK() {

        Map response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("/api%s/view/%s".formatted(mainPath, idMain))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(idMain, response.get("id"));

        Map responseChild = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .when()
            .get("/api%s/view/%s".formatted(childPath, idChild))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(idChild, responseChild.get("id"));
        assertEquals(idMain, responseChild.get("id_at_testmain"));
    }

    @Test
    @DisplayName("测试更新主表和子表记录后数据是否正确")
    void testUpdateMainAndChildren() {
        Map main = Map.of(
            "v_name", "main2",
            childAlias, List.of(
                Map.of("v_name", "child2")
            )
        );

        given()
            .contentType("application/json")
            .body(main)
            .when()
            .post("/api%s/update/%s".formatted(mainPath, idMain))
            .then()
            .statusCode(200)
            .body(is("1"));

        Map<String, ?> map = testMain.view(idMain);
        assertEquals("main2", map.get("v_name"));

        List<Map> childTableList = testMain.getChildTableList(idMain, childAlias, null);
        assertEquals("child2", childTableList.get(0).get("v_name"));
    }

    @Test
    @DisplayName("测试获取子表记录列表")
    void testGetChildTableList() {
        List<Map> response = given()
            .queryParam("noPage", true)
            .get("/api%s/view/%s/child/%s".formatted(mainPath, idMain, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertNotNull(response.stream().filter(it -> it.get("id").equals(idChild)));
    }

    @Test
    @DisplayName("测试子表的增删改查操作")
    void testChildCRUD() {
        String child2 = given()
            .contentType("application/json")
            .body(Map.of("v_name", "child2"))
            .when()
            .post("/api%s/update/%s/child/%s/create".formatted(mainPath, idMain, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertNotNull(child2);

        Map child2Map = given()
            .get("/api%s/view/%s/child/%s/view/%s".formatted(mainPath, idMain, childAlias, child2))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(idMain, child2Map.get("id_at_testmain"));
        assertEquals("child2", child2Map.get("v_name"));

        String editCount = given()
            .contentType("application/json")
            .body(Map.of("v_name", "child22"))
            .when()
            .post("/api%s/update/%s/child/%s/update/%s".formatted(mainPath, idMain, childAlias, child2))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", editCount);

        Map child2Map2 = given()
            .get("/api%s/view/%s/child/%s/view/%s".formatted(mainPath, idMain, childAlias, child2))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(idMain, child2Map2.get("id_at_testmain"));
        assertEquals("child22", child2Map2.get("v_name"));

        String delCount = given()
            .get("/api%s/update/%s/child/%s/delete/%s".formatted(mainPath, idMain, childAlias, child2))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        assertEquals("1", delCount);

        given()
            .get("/api%s/view/%s/child/%s/view/%s".formatted(mainPath, idMain, childAlias, child2))
            .then()
            .statusCode(204);

    }

    @Test
    @DisplayName("测试批量操作子表记录")
    void testBatch() {
        System.out.println(idMain);
        System.out.println(idChild);

        List children = List.of(
            Map.of("id", idChild, "v_name", "child1"),
            Map.of("v_name", "child2"),
            Map.of("v_name", "child3")
        );

        BatchResult result = given()
            .contentType("application/json")
            .body(children)
            .when()
            .post("/api%s/update/%s/child/%s".formatted(mainPath, idMain, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(2, result.getCreate());
        assertEquals(1, result.getUpdate());
        assertEquals(0, result.getDelete());

        List children2 = List.of(
            Map.of("id", idChild, "v_name", "child1")
        );

        BatchResult result2 = given()
            .contentType("application/json")
            .body(children2)
            .when()
            .post("/api%s/update/%s/child/%s".formatted(mainPath, idMain, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(0, result2.getCreate());
        assertEquals(1, result2.getUpdate());
        assertEquals(2, result2.getDelete());

        //这种情况会直接清空子表数据
        BatchResult result3 = given()
            .contentType("application/json")
            .body(List.of())
            .when()
            .post("/api%s/update/%s/child/%s".formatted(mainPath, idMain, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(0, result3.getCreate());
        assertEquals(0, result3.getUpdate());
        assertEquals(1, result3.getDelete());
    }

    @Test
    @DisplayName("测试删除主表记录时子表记录是否也被删除")
    void testDeleteMainTable() {
        String mainID = testMain.create(Map.of("v_name", "main"));

        BatchResult result = given()
            .contentType("application/json")
            .body(List.of(
                Map.of("v_name", "child1"),
                Map.of("v_name", "child2"),
                Map.of("v_name", "child3")
            ))
            .when()
            .post("/api%s/update/%s/child/%s".formatted(mainPath, mainID, childAlias))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(3, result.getCreate());

        List rows = (List) databaseOperations.query("select * from test.testchildren where id_at_testmain = ? ", mainID);

        assertEquals(3, rows.size());

        given()
            .get("/api%s/delete/%s".formatted(mainPath, mainID))
            .then()
            .statusCode(200);

        List rows2 = (List) databaseOperations.query("select * from test.testchildren where id_at_testmain = ? ", mainID);

        assertEquals(0, rows2.size());
    }
}

@Path("/testmain")
class TestMain extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IChildrenAbility {

    @Inject
    TestChildren testChildren;

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testmain";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("v_name").setSymbolType(QueryItem.SymbolType.LIKE)
        );
    }

    @Override
    public List<ChildTableInfo> getChildren() {
        return List.of(
            testChildren.toChildTable("id_at_testmain").setAutoDelete().setChildAlias("child")
        );
    }
}

@Path("/testchildren")
class TestChildren extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, IChildAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testchildren";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID_V7)
            .addColumn(Column.of("v_name"))
            .addColumn(Column.of("id_at_testmain"));
    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id")
        );
    }
}
