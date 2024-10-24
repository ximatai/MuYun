package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.FileServerConfig;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestFileDownload {

    @Inject
    IFileService service;

    @Inject
    FileServerConfig config;
    
    String fileName;
    String fileContent;
    File tempFile;
    
    @BeforeEach
    public void setup() throws IOException {
        fileName = "ZZZZZZ.txt";
        fileContent = "hello world";
        tempFile = File.createTempFile(fileName.split("\\.")[0], fileName.split("\\.")[1]);
        FileOutputStream fos = new FileOutputStream(tempFile);
        fos.write(fileContent.getBytes());
        fos.close();
    }

    @Test
    void testFileDownload() throws InterruptedException {
        String id = service.save(tempFile, fileName).split("@")[0];
        Thread.sleep(1000);
        File file = service.get(id);
        String filePath = file.getPath();
        System.out.println(filePath);
        // 错误原因: 目标目录中已经存在了同名文件，导致无法创建目标文件的副本
        File file2 = service.get(id);
        String filePath2 = file2.getPath();
        System.out.println(filePath2);
    }
    
}
