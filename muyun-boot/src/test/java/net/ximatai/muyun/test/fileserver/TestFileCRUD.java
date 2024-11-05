package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import io.vertx.core.json.JsonObject;
import jakarta.inject.Inject;
import net.ximatai.muyun.fileserver.FileInfoEntity;
import net.ximatai.muyun.fileserver.FileServerConfig;
import net.ximatai.muyun.fileserver.IFileService;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestFileCRUD {

    @Inject
    IFileService service;

    @Inject
    FileServerConfig config;

    @Test
    void testCRUD() throws IOException, InterruptedException {
        // save()
        int fileNameInt = getRandomInt();
        String fileName = fileNameInt + ".txt";
        File tempFile = File.createTempFile(fileName, ".txt");
        FileOutputStream fos = new FileOutputStream(tempFile);
        int ctx1 = getRandomInt();
        String fileContent = "";
        fileContent += ctx1 + "\n";
        int ctx2 = getRandomInt();
        fileContent += String.valueOf(ctx2);
        fos.write(fileContent.getBytes());
        fos.close();
        String id = service.save(tempFile, fileName).split("@")[0];

        String filePathWithN = config.uploadPath() + id + "-n";
        String filePathWithO = config.uploadPath() + id + "-o";
        Path pathN = Paths.get(filePathWithN);
        Path pathO = Paths.get(filePathWithO);
        Thread.sleep(2000);
        assertTrue(Files.exists(pathN));
        assertTrue(Files.exists(pathO));

        String name = Files.readString(pathN);
        String content = Files.readString(pathO);
        assertEquals(fileName, name);
        assertEquals(fileContent, content);

        // get()
        File file = service.get(id);
        if (file == null) System.out.println("file is null");
        assert file != null;
        String newFileName = file.getName();
        assertEquals(fileName.substring(0, 10), newFileName.substring(0, 10));
        String newFileContent = Files.readString(Paths.get(file.getPath()));
        assertEquals(fileContent, newFileContent);

        // info
        FileInfoEntity entity = service.info(id);
        JsonObject jsonObject = entity.toJson();
        String infoName = jsonObject.getString("name");
        long infoSize = jsonObject.getLong("size");
        String infoSuffix = jsonObject.getString("suffix");
        assertEquals(fileName, infoName);
        assertEquals(fileContent.length(), infoSize);
        assertEquals("txt", infoSuffix);

        // delete()
        boolean isDeleted = service.delete(id);
        assertTrue(isDeleted);
        boolean isDeleted2 = file.delete();
        assertTrue(isDeleted2);

        tempFile.deleteOnExit();
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
