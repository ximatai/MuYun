package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import net.ximatai.muyun.ability.IFileAbility;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestFileAbility {

    // 文件名
    String fileName;
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
        fileContent += ctx1 + "\n";
        int ctx2 = getRandomInt();
        fileContent += String.valueOf(ctx2);
        fos.write(fileContent.getBytes());
        fos.close();
    }

    @Test
    @DisplayName("测试上传下载文件")
    void testUploadAndDownload() {

        // 文件上传
        Response response = given()
            .multiPart("file", tempFile)
            .when()
            .post("/api/platform/testfile/upload")
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();

        String fileID = response.getBody().asString();

        String id = given()
            .contentType("application/json")
            .body(Map.of("v_name", "test", "files_att", List.of(fileID)))
            .when()
            .post("/api/platform/testfile/create")
            .then()
            .statusCode(200)
            .extract()
            .asString();

        // 下载文件
        Response response2 = given()
            .when()
            .queryParam("fileID", fileID)
            .get("/api/platform/testfile/download/" + id)
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();

        String downloadContent = response2.getBody().asString();
        // 验证文件内容是否相同
        assertEquals(fileContent, downloadContent);
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

@Path("/platform/testfile")
class TestFileAbilityController extends BaseScaffold implements IFileAbility {

    @Inject
    IFileService fileService;

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
            .setPrimaryKey(PresetColumn.ID_POSTGRES_UUID)
            .addColumn(Column.of("v_name").setType(ColumnType.VARCHAR))
            .addColumn("file_att")
            .addColumn("files_att");
    }

    @Override
    public IFileService getFileService() {
        return fileService;
    }

    @Override
    public List<String> fileColumns() {
        return List.of(
            "file_att",
            "files_att"
        );
    }
}
