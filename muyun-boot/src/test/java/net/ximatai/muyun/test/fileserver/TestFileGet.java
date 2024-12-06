package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.FileServerConfig;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestFileGet {
    
    @Inject
    IFileService service;
    
    @Inject
    FileServerConfig config;
    
    @Test
    @DisplayName("测试文件的移动和重命名操作")
    public void test() throws IOException {
        String fileName = "MayDay";
        File tempFile = File.createTempFile(fileName, ".txt");
        String split = tempFile.getAbsolutePath().toString().split(fileName)[0];
        System.out.println(split);
        String uid = generateBsyUid();
        String directoryPath = split + uid;
        File directory = new File(directoryPath);
        // 检查文件夹是否存在，如果不存在则创建  
        if (!directory.exists()) {
            boolean result = directory.mkdirs(); // mkdirs() 会创建所有必需的父目录
            if (result) {
                System.out.println("文件夹创建成功: " + directoryPath);
            } else {
                System.out.println("文件夹创建失败: " + directoryPath);
            }
        } else {
            System.out.println("文件夹已存在: " + directoryPath);
        }
        System.out.println(tempFile.getAbsolutePath());
        System.out.println(tempFile.getName());
        Path sourcePath = Paths.get(tempFile.getAbsolutePath());
        Path targetDir = Paths.get(directory.getAbsolutePath());
        String newFileName = fileName + ".txt";
        Path targetPath = targetDir.resolve(newFileName);
        try {
            // 移动并重命名文件  
            Files.move(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
            System.out.println("文件移动并重命名成功: " + sourcePath + " -> " + targetPath);
        } catch (IOException e) {
            System.err.println("文件移动或重命名失败: " + e.getMessage());
        }

    }
    
    @Test
    @DisplayName("测试文件的创建、写入、保存和获取操作")
    public void test2() throws IOException {
        // 文件路径  
        Path filePath = Paths.get("./OctDay.txt");

        // 要写入的内容（作为字符串列表）  
        List<String> lines = Arrays.asList(
            "这是第一行内容",
            "这是第二行内容",
            "这是第三行内容"
        );

        try {
            // 将字符串列表转换为字节序列（使用UTF-8编码）  
            Files.write(filePath, lines, StandardCharsets.UTF_8);
            System.out.println("文件创建并写入成功: " + filePath);
        } catch (IOException e) {
            System.err.println("文件创建或写入失败: " + e.getMessage());
        }
        
        File file = filePath.toFile();
        String id = service.save(file, "OctDay.txt");
        
        File fileGet = service.get(id);
        System.out.println(fileGet.getName());
        System.out.println(file.getName());
        System.out.println(fileGet.getAbsolutePath());
        assertEquals(fileGet.getName(), file.getName());
        service.delete(id);
    }

    private String generateBsyUid() {
        UUID uid = UUID.randomUUID();
        return "bsy-" + uid;
    }
}
