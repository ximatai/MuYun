package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.eventbus.EventBus;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IDataBroadcastAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.model.DataChangeChannel;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
class TestDataBroadcastAbility {

    @Inject
    EventBus eventBus;

    @Inject
    TestDataBroadcastAbilityController testDataBroadcastAbilityController;

    private String path = "/TestDataBroadcastAbility";

    @Test
    void test() throws InterruptedException {
        DataChangeChannel channel = new DataChangeChannel(testDataBroadcastAbilityController);

        eventBus.consumer(channel.getAddress(), message -> {
            System.out.println(message.headers().get("type"));
            System.out.println(message.body());
        });

        eventBus.consumer(channel.getAddressWithType(DataChangeChannel.Type.CREATE), message -> {
            System.out.println("create " + message.body());
        });

        eventBus.consumer(channel.getAddressWithType(DataChangeChannel.Type.UPDATE), message -> {
            System.out.println("update " + message.body());
        });

        eventBus.consumer(channel.getAddressWithType(DataChangeChannel.Type.DELETE), message -> {
            System.out.println("delete " + message.body());
        });

        Map<String, Object> request = Map.of("name", "test");

        String id = given()
            .contentType("application/json")
            .body(request)
            .when()
            .post("%s/create".formatted(path))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();
        //.body(is(id));

        given()
            .contentType("application/json")
            .body(Map.of("name", "test2"))
            .when()
            .post("%s/update/%s".formatted(path, id))
            .then()
            .statusCode(200)
            .extract()
            .response()
            .asString();

        given().get("%s/delete/%s".formatted(path, id)).then().statusCode(200);

        given().get("/test/view/" + id).then().statusCode(404);

    }

}

@Path("/TestDataBroadcastAbility")
class TestDataBroadcastAbilityController extends Scaffold implements ICURDAbility, ITableCreateAbility, IDataBroadcastAbility {

    DataChangeChannel dataChangeChannel = new DataChangeChannel(this);

    @Override
    public String getSchemaName() {
        return "test";
    }

    @Override
    public String getMainTable() {
        return "testdatabroadcastability";
    }

    @Override
    public TableWrapper fitOutTable() {
        return TableWrapper.withName(getMainTable())
            .setSchema(getSchemaName())
            .setPrimaryKey(Column.ID_POSTGRES)
            .addColumn(Column.of("name").setType("varchar"))
            .addColumn(Column.of("t_create").setDefaultValue("now()"));

    }

    @Override
    public DataChangeChannel getDataChangeChannel() {
        return dataChangeChannel;
    }
}
