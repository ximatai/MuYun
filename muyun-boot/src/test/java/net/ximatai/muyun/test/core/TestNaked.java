package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.common.mapper.TypeRef;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDatabaseAbility;
import net.ximatai.muyun.ability.IMetadataAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ISelectAbility;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestNaked {

    @Test
    void test() {
        given()
            .queryParam("sort", "t_create,desc")
            .get("/api/demo/view")
            .then()
            .statusCode(200)
            .extract()
            .as(new TypeRef<>() {
            });
    }
}

@Path("/demo")
class Demo implements IDatabaseAbility, IMetadataAbility, ITableCreateAbility, ISelectAbility {

    @Inject
    IDatabaseOperations databaseOperations;

    @PostConstruct
    void init() {
        create(getDatabaseOperations());
    }

    @Override
    public String getSchemaName() {
        return "public";
    }

    @Override
    public String getMainTable() {
        return "demo";
    }

    @Override
    public IDatabaseOperations getDatabaseOperations() {
        return databaseOperations;
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn("v_name")
            .addColumn("i_age");
    }
}
