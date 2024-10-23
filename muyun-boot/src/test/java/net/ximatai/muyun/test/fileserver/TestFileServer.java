package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import io.vertx.core.json.JsonObject;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
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
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestFileServer {

    // 文件名
    String fileName;
    String id;
    // 文件内容
    String fileContent = "";
    // 临时文件
    File tempFile;

    @BeforeEach
    public void setup() throws IOException {
        int fileNameInt = getRandomInt();
        fileName = String.valueOf(fileNameInt);
        tempFile = File.createTempFile(fileName, ".txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        int ctx1 = getRandomInt();
        fileContent += String.valueOf(ctx1 + "\n");
        int ctx2 = getRandomInt();
        fileContent += String.valueOf(ctx2);
        fos.write(fileContent.getBytes());
        fos.close();
    }

    @Test
    void testFileProcess() {
        // 文件上传
        Response response = given()
            .multiPart("file", tempFile)
            .when()
            .post("/fileServer/upload")
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();

        id = response.getBody().asString();

        // 下载文件
        Response response2 = given()
            .when()
            .get("/fileServer/download/" + id)
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();

        String downloadContent = response2.getBody().asString();
        // 验证文件内容是否相同
        assertEquals(fileContent, downloadContent);

        // 读取文件info
        Response response3 = given()
            .when()
            .get("/fileServer/info/" + id)
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
        String deleteUrl = "/fileServer/delete/" + id;
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
