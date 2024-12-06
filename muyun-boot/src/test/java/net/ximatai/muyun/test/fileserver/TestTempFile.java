package net.ximatai.muyun.test.fileserver;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TestTempFile {

    @Test
    @DisplayName("测试文件的长度")
    public void test() throws IOException {
        String fileName = "五月天.txt";
        System.out.println(fileName.length());
    }
}
