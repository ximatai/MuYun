package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.junit.QuarkusTest;
import io.restassured.response.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static io.restassured.RestAssured.given;

@QuarkusTest
public class TestChinese {
    
    String fileName;
    
    File fileFirst;
    
    
    
    @BeforeEach
    public void setup() throws IOException {
        fileName = "五月天.txt";
        Path filePath = Paths.get("./" + fileName);
        List<String> lines = Arrays.asList(
            "我不愿让你一个人",
            "一个人在人海浮沉",
            "我不愿你独自走过风雨的时分"
        );
        Files.write(filePath, lines, StandardCharsets.UTF_8);
        fileFirst = filePath.toFile();
    }
    
    @Test
    public void test() throws IOException {
//        Response response = given()
//            .multiPart("file", fileFirst)
//            .when()
//            .post("/fileServer/upload")
//            .then()
//            .log().all()
//            .statusCode(200)
//            .extract()
//            .response();
//        
//        String id = response.getBody().asString();
//        System.out.println(id);        
        
//        Response response2 = given()
//            .when()
//            .get("/fileServer/download/"+id)
//            .then()
//            .log().all()
//            .statusCode(200)
//            .extract()
//            .response();

        //String downloadContent = response2.getBody().asString();
        //System.out.println(downloadContent);

        LocalDateTime currentTime = LocalDateTime.now();
        System.out.println(currentTime);
        
    }
}
