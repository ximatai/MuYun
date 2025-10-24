package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.platform.controller.ModuleController;
import net.ximatai.muyun.platform.model.ModuleAction;
import net.ximatai.muyun.platform.model.ModuleConfig;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.List;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertFalse;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestAuthAbility {

    @Inject
    MuYunConfig config;

    @Inject
    ModuleController moduleController;

    @BeforeAll
    void setUp() {
        moduleController.register(
            ModuleConfig.ofName("test")
                .setAlias("test")
                .setTable("test")
                .addAction(ModuleAction.VIEW)
        );
    }

    @Test
    @DisplayName("验证获取所有操作权限")
    void testActions() {
        List<String> actions = given()
            .header("userID", config.superUserId())
            .get("/api/platform/test/actions")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertFalse(actions.isEmpty());
    }

    @Test
    @DisplayName("验证获取单个记录的操作权限")
    void testActionsForOne() {
        String id = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "测试"
            ))
            .when()
            .post("/api/platform/test/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        List<String> actions = given()
            .header("userID", "1")
            .get("/api/platform/test/actions/%s".formatted(id))
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {

            });

        assertFalse(actions.isEmpty());
    }
}

@Path("/platform/test")
class TestAuthAbilityController extends BaseScaffold {

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "test";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn(Column.of("v_name").setType(ColumnType.VARCHAR));
    }

}
