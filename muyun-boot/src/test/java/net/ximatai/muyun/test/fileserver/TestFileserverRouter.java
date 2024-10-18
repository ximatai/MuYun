package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class TestFileserverRouter {

    // 文件名
    String nameF;
    String uid;
    // 文件内容
    String contextF = "";
    // 临时文件
    File tempFile;

    @BeforeEach
    public void setup() {
        int fileNameInt = getRandomInt();
        nameF = String.valueOf(fileNameInt);
        try {
            tempFile = File.createTempFile(nameF, ".txt");
            FileOutputStream fos = new FileOutputStream(tempFile);
            int ctx1 = getRandomInt();
            contextF += String.valueOf(ctx1 + "\n");
            int ctx2 = getRandomInt();
            contextF += String.valueOf(ctx2);
            fos.write(contextF.getBytes());
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Test
    void testFileProcess() {
        // 文件上传
        Response response = given()
            .multiPart("file", tempFile)
            .when()
            .post("/fs/form")
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();
        // String jsonResponse = response.getBody().asString();
        // JsonObject jsonObject = new JsonObject(jsonResponse);

        String uid = response.getBody().asString();

        // 下载文件
        Response response2 = given()
            .when()
            .get("/fs/download/" + uid)
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();

        String downloadContent = response2.getBody().asString();
        // 验证文件内容是否相同
        assertEquals(contextF, downloadContent);

        // 文件内容可以再info接口中验证
        // assertEquals(tempFile.getName(), jsonObject.getString("fileName"));

        // 读取文件info
        Response response3 = given()
            .when()
            .get("/fs/info/" + uid)
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();
        String jsonResponse = response3.getBody().asString();
        JsonObject jsonObject = new JsonObject(jsonResponse);
        String fileName = jsonObject.getString("name");
        assertEquals(tempFile.getName(), fileName);
    }

    @AfterEach
    public void tearDown() {
        String deleteUrl = "/fs/delete/" + uid;
        given()
            .when()
            .get(deleteUrl)
            .then()
            .statusCode(200);

        tempFile.delete();
    }

    /**
     * @return 返回一个随机的正数
     */
    int getRandomInt() {
        Random rand = new Random();
        int num = rand.nextInt();
        while (num < 1000000000) {
            num = rand.nextInt();
        }
        return num;
    }
}
