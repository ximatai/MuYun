package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestOnlineUser {

    @Inject
    MuYunConfig config;

    @Test
    void testOnline() {
        String deviceID = given()
            .header("userID", config.superUserId())
            .get("/platform/online/iAmHere")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        Assertions.assertNotNull(deviceID);

    }

}
