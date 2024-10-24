package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestTempFile {
    
    @Test
    public void test() throws IOException {
        String fileName = "MaNing.txt";
        File tempFile = File.createTempFile(fileName.split("\\.")[0], "." + fileName.split("\\.")[1]);
    }
}
