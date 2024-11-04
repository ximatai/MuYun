package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.Assert.assertEquals;

@QuarkusTest
//@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestRange {
    
    private String path = "/test_range";
    
    @Inject
    IDatabaseOperations databaseOperations;
    
    @Inject
    TestRangeController controller;
    
    String tableName;
    
    @BeforeEach 
    void setUp(){
        tableName = controller.getMainTable();
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(tableName));
        controller.create(Map.of("id", "1", "name", "test1", "t_create", "2024-01-01 12:00:00"));
    }
    
    @Test
    @DisplayName("测试日期查询")
    void test(){
        Map<String, ?> request = Map.of("t_create", new String[]{"2024-01-01", "2024-01-01"});

        PageResult<Map> response = given()
            .contentType("application/json")
            .queryParam("noPage", true)
            .body(request)
            .when()
            .post("%s/view".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>(){
                
            });
        
        assertEquals(1, response.getTotal());
    }
}

@Path("/test_range")
class TestRangeController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility {
    
    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test_table_range";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))  // 字段名
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public List<QueryItem> queryItemList() {
        return List.of(
            QueryItem.of("id"),
            QueryItem.of("id").setAlias("no_id").setSymbolType(QueryItem.SymbolType.NOT_EQUAL),
            QueryItem.of("id").setAlias("in_id").setSymbolType(QueryItem.SymbolType.IN),
            QueryItem.of("id").setAlias("not_in_id").setSymbolType(QueryItem.SymbolType.NOT_IN),
            QueryItem.of("name").setSymbolType(QueryItem.SymbolType.LIKE),
            QueryItem.of("t_create").setTime(true).setSymbolType(QueryItem.SymbolType.RANGE)
        );
    }
}
