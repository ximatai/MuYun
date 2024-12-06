package net.ximatai.muyun.test.plaform;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.core.config.MuYunConfig;
import net.ximatai.muyun.core.exception.MyException;
import net.ximatai.muyun.platform.controller.RegionController;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestRegionController {
    @Inject
    MuYunConfig config;

    @Inject
    RegionController regionController;

    @Test
    @DisplayName("测试创建区域时未提供 ID 抛出 NullPointerException")
    void testCreateWithIDBlank() { // 必须提供 id
        assertThrows(NullPointerException.class, () -> {
            regionController.create(Map.of(
                "v_name", "test"
            ));
        });
    }

    @Test
    @DisplayName("测试创建、更新和删除区域")
    void testCreateWithID() {
        String id = regionController.create(Map.of(
            "id", "test_region",
            "v_name", "test"
        ));

        assertEquals("test_region", id);

        assertThrows(MyException.class, () -> {
            regionController.create(Map.of(
                "id", "test_region",
                "v_name", "test"
            ));
        });

        regionController.update(id, Map.of(
            "id", "test_new",
            "v_name", "test"
        ));

        assertNull(regionController.view(id));
        assertNotNull(regionController.view("test_new"));

        String id2 = regionController.create(Map.of(
            "id", "test_region2",
            "v_name", "test"
        ));

        assertThrows(MyException.class, () -> {
            regionController.update(id2, Map.of(
                "id", "test_new",
                "v_name", "test"
            ));
        });
    }

    @Test
    @DisplayName("测试获取 ID 和名称映射以及通过 ID 获取名称")
    void testIdMapName() throws InterruptedException {
        String id1 = regionController.create(Map.of(
            "id", UUID.randomUUID().toString(),
            "v_name", "test1"
        ));

        Map<String, String> stringStringMap = regionController.idMapName();

        assertEquals("test1", stringStringMap.get(id1));

        String id2 = regionController.create(Map.of(
            "id", UUID.randomUUID().toString(),
            "v_name", "test2"
        ));

        Thread.sleep(500);

        assertEquals("test2", regionController.getNameById(id2));
    }
}
