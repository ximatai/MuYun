package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.ISoftDeleteAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.base.BaseBusinessTable;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestBaseColumns {
    private String path = "/TestBaseColumns";

    @Test
    @DisplayName("验证创建记录时自动生成的审计列存在")
    void test() {
        String id = given()
            .contentType("application/json")
            .body(Map.of(
                "v_test", "test"
            ))
            .post("/api%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Map row = given()
            .get("/api%s/view/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        Assertions.assertTrue(row.containsKey("id_at_auth_user__create"));
        Assertions.assertTrue(row.containsKey("id_at_auth_user__update"));
        Assertions.assertTrue(row.containsKey("id_at_auth_user__delete"));
        Assertions.assertTrue(row.containsKey("t_delete"));
    }

}

@Path("/TestBaseColumns")
class TestBaseColumnsController extends Scaffold implements ICURDAbility, ITableCreateAbility, ISoftDeleteAbility {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testbasecolumns";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setInherit(BaseBusinessTable.TABLE)
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn(Column.of("v_test"));
    }

}
