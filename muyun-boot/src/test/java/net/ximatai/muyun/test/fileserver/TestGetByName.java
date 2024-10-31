package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.FileServerConfig;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestGetByName {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    FileServerConfig config;

    @Inject
    IFileService fileService;

    private String folderPath;

    @BeforeEach
    public void init() {
        // 检查配置的文件夹是否存在
        folderPath = config.uploadPath();
        if (!folderPath.endsWith("/") && !folderPath.endsWith("\\")) {
            folderPath = folderPath + "/";
        }
        Path path = Paths.get(folderPath);
        try {
            if (Files.notExists(path)) {
                Files.createDirectories(path);
                System.out.println("文件夹已创建: " + folderPath);
            } else {
                System.out.println("文件夹已存在: " + folderPath);
            }
        } catch (Exception e) {
            logger.error("创建文件夹失败", e);
        }
    }

    @Test
    public void test() throws IOException {
        String fileName = "五月天.txt";
        Path filePath = Paths.get(folderPath, fileName);
        String data = "当我和这个世界不一样，那就让我不一样";
        byte[] dataBytes = data.getBytes(StandardCharsets.UTF_8);
        Files.write(filePath, dataBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        File file = fileService.get2(fileName);
        assertNotNull(file);
        Response response = given()
            .when()
            .get("/fileServer/download/" + fileName)
            .then()
            .log().all()
            .statusCode(200)
            .extract()
            .response();
        String nameByGet2 = response.getHeader("Content-Disposition").split("''")[1];
        String nameDecode = URLDecoder.decode(nameByGet2, StandardCharsets.UTF_8);
        assertEquals(fileName, nameDecode);
    }
}
