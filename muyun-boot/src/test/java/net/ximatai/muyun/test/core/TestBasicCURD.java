package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestBasicCURD {

    @Inject
    IDatabaseOperations databaseOperations;  // 数据库配置注入

    @Inject
    TestBasicCURDController testController;  // 注入controller

    String tableName;  // 表名

    List<String> ids;  // 存放id的List

    @BeforeEach
    void setUp() {
        tableName = testController.getMainTable();  // 表名从controller中拿到
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(tableName));  // 快速删除表中所有数据

        var id1 = testController.create(Map.of("id", "1", "name", "test1"));  // 用controller新增数据
        var id2 = testController.create(Map.of("id", "2", "name", "test2"));
        var id3 = testController.create(Map.of("id", "3", "name", "test3"));

        ids = List.of(id1, id2, id3);   // ids存入新增数据的id

//        var id1 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test1"));
//        var id2 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test2"));
//        var id3 = databaseOperations.insert("insert into test_table (name) values (:name) ", Map.of("name", "test3"));

    }

    @Test
    @DisplayName("查询分页结果")
    void testPageView() {
        String id = ids.getFirst();  // 得到ids中第一个id
        PageResult response = given()  // response得到一个分页结果
            .queryParam("page", 1) // 第1页
            .queryParam("size", 2)  // 每页大小为2
            .get("/test/view")  // 查询
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.getTotal(), 3);  // 共计3条数据
        assertEquals(response.getList().size(), 2);  // 有两页
        assertEquals(response.getPage(), 1);  // 当前页面为1
        assertEquals(response.getSize(), 2);  // 页面大小为2
    }

    @Test
    @DisplayName("测试排序功能")
    void testPageViewSort() {
        PageResult<HashMap> response = given()
            .queryParam("page", 1)
            .queryParam("size", 2)
            .queryParam("sort", "t_create")  // 根据t_create排序
            .get("/test/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(response.getList().getFirst().get("id"), "1");
    }

    @Test
    @DisplayName("测试降序排序")
    void testPageViewSortDesc() {
        PageResult<HashMap> response = given()
            .queryParam("page", 1)
            .queryParam("size", 2)
            .queryParam("sort", "t_create,desc")  // 根据创建时间升序
            .queryParam("sort", "name,desc")  // 根据name降序
            .get("/test/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals("3", response.getList().getFirst().get("id"));
    }

    @Test
    @DisplayName("测试数据新增功能")
    void testCreate() {
        // 新增数据
        String id = "666";
        Map<String, String> request = Map.of("id", id, "name", "test", "name2", "test2");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/create")
            .then()
            .statusCode(200)
            .body(is(id));

        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertEquals(request.get("id"), e.get("id"));
        assertEquals(request.get("name"), e.get("name"));
        assertNull(e.get("name2"));  // 这里的新字段name2为null
        assertNotNull(e.get("t_update"));
        assertNotNull(e.get("t_create"));
    }

    @Test
    @DisplayName("测试批量新增数据")
    void testBatchCreate() {
        List<String> ids = given()
            .contentType("application/json")
            .body(List.of(
                Map.of("name", "test", "name2", "test2"),
                Map.of("name", "test", "name2", "test2"),
                Map.of("name", "test", "name2", "test2")
            ))
            .when()
            .post("/test/batchCreate")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertEquals(ids.size(), 3);

        Map row1 = (Map) databaseOperations.row("select * from %s where id = ? ".formatted(tableName), ids.get(0));
        Map row2 = (Map) databaseOperations.row("select * from %s where id = ? ".formatted(tableName), ids.get(1));
        Map row3 = (Map) databaseOperations.row("select * from %s where id = ? ".formatted(tableName), ids.get(2));

        assertNotNull(row1);
        assertNotNull(row2);
        assertNotNull(row3);

        assertEquals("test", row1.get("name"));
        assertEquals("test", row2.get("name"));
        assertEquals("test", row3.get("name"));
    }

    @Test
    @DisplayName("测试更新数据")
    void testUpdate() {
        String id = ids.getFirst();
        Map<String, String> request = Map.of("name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(200)
            .body(is("1"));

        // 从数据库表中查询一条数据
        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertNotNull(e.get("t_update"));
        assertEquals(request.get("name"), e.get("name"));
    }

    @Test
    @DisplayName("测试修改id不存在的数据，返回404")
    void testUpdateNotFound() {
        String id = "666";
        Map<String, String> request = Map.of("name", "test");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(404);
    }

    @Test
    @DisplayName("测试修改不存在的字段")
    void testUpdateFieldNotExists() {
        String id = ids.getFirst();
        Map<String, String> request = Map.of("unknown", "field");
        given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("/test/update/" + id)
            .then()
            .statusCode(200);
    }

    @Test
    @DisplayName("测试查询数据")
    void testGet() {
        String id = ids.getFirst();
        HashMap response = given()
            .get("/test/view/" + id)
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        Map e = (Map) databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id));

        assertEquals(e.get("name"), response.get("name"));
        assertEquals(e.get("id"), response.get("id"));
    }

    @Test
    @DisplayName("测试查询不存在的数据")
    void testGetNotFound() {
        String id = "666";
        given()
            .get("/test/view/" + id)
            .then()
            .statusCode(204);
    }

    @Test
    @DisplayName("测试删除数据")
    void testDelete() {
        String id = ids.getFirst();
        given()
            .get("/test/delete/" + id)
            .then()
            .statusCode(200);

        assertNull(
            databaseOperations.row("select * from %s where id = :id ".formatted(tableName), Map.of("id", id))
        );
    }

    @Test
    @DisplayName("测试删除id不存在的数据，返回404")
    void testDeleteNotFound() {
        String id = "666";
        given()
            .get("/test/delete/" + id)
            .then()
            .statusCode(404);
    }

}

@Path("/test")  // 访问路径
class TestBasicCURDController extends Scaffold implements ICURDAbility, ITableCreateAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }  // 模式名

    @Override
    public String getMainTable() {
        return "test_table";
    }  // 表名

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))  // 字段名
            .addColumn(Column.of("t_create"))  
            .addColumn(Column.of("t_update"));

    }
}
