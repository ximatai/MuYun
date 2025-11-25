package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.curd.std.IDataCheckAbility;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestDataCheckAbility {
    @Inject
    MuYunConfig config;

    @Test
    @DisplayName("测试提交空字符串的v_name字段")
    void testStringBlank() {
        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", " ",
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(500)
            .extract()
            .asString();

        Assertions.assertEquals("数据项[名称]要求为必填", result);
    }

    @Test
    @DisplayName("测试提交缺少v_title字段")
    void testStringNull() {
        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "name",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(500)
            .extract()
            .asString();

        Assertions.assertEquals("数据项[标题]要求为必填", result);
    }

    @Test
    @DisplayName("测试提交空列表的ids_test字段")
    void testListEmpty() {
        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", "name",
                "v_title", "test_title",
                "ids_test", List.of()
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(500)
            .extract()
            .asString();

        Assertions.assertEquals("数据项[数组测试]要求为必填", result);
    }

    @Test
    @DisplayName("测试创建重复的v_name数据")
    void testDuplicateDataCreate() {
        String name = UUID.randomUUID().toString();
        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", name,
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", name,
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(500)
            .extract()
            .asString();

        Assertions.assertEquals("数据项[名称]已存在相同的数据", result);
    }

    @Test
    @DisplayName("测试更新为重复的v_name数据")
    void testDuplicateDataUpdate() {
        String name = UUID.randomUUID().toString();
        String id = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", name,
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", name,
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/update/%s".formatted(id))
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String id2 = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", UUID.randomUUID().toString(),
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        String result = given()
            .header("userID", config.superUserId())
            .contentType("application/json")
            .body(Map.of(
                "v_name", name,
                "v_title", "test_title",
                "ids_test", List.of(1, 2, 3)
            ))
            .when()
            .post("/api/TestDataCheckAbility/update/%s".formatted(id2))
            .then()
            .statusCode(500)
            .extract()
            .asString();

        Assertions.assertEquals("数据项[名称]已存在相同的数据", result);
    }
}

@Path("/TestDataCheckAbility")
class TestDataCheckAbilityController extends BaseScaffold implements IDataCheckAbility {

    @Override
    public String getMainTable() {
        return "testdatacheckability";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper.setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn("v_name", "名称", null, false)
            .addColumn("v_title", "标题", null, false)
            .addColumn("ids_test", "数组测试", null, false)
            .addIndex("v_name", true);
    }
}
