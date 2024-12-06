package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.FileServerConfig;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestFolderExists {

    final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    FileServerConfig config;

    private String getUploadPath() {
        String uploadPath = config.uploadPath();
        if (!uploadPath.endsWith("/") && !uploadPath.endsWith("\\")) {
            uploadPath = uploadPath + "/";
        }
        return uploadPath;
    }

    @Test
    @DisplayName("测试文件夹是否存在，如果不存在则创建它")
    void testFolder() {
        String folderPath = getUploadPath();
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
}
