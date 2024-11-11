package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.ability.curd.std.ICustomSelectSqlAbility;
import net.ximatai.muyun.ability.curd.std.IQueryAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.ColumnType;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestCustomSqlQuery {

    private String path = "/TestCustomSqlQuery";

    @Inject
    IDatabaseOperations databaseOperations;

    @Inject
    TestCustomSqlQueryController testController;

    String tableName;

    @BeforeEach
    void setUp() {
        tableName = testController.getMainTable();
        databaseOperations.execute("TRUNCATE TABLE %s".formatted(tableName));

        testController.create(Map.of("id", "1", "name", "test1", "t_create", "2024-01-01 12:00:00"));
    }

    @Test
    void testView() {

        Map row = given()
            .contentType("application/json")
            .get("%s/view/%s".formatted(path, "1"))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });

        assertNotNull(row);
        assertEquals("xxx", row.get("x"));
    }

}

@Path("/TestCustomSqlQuery")
class TestCustomSqlQueryController extends Scaffold implements ICURDAbility, ITableCreateAbility, IQueryAbility, ICustomSelectSqlAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testcustomsqlquery";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType(ColumnType.VARCHAR))
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

    @Override
    public String getCustomSql() {
        return "select *,'xxx' as x from test.testcustomsqlquery";
    }
}
