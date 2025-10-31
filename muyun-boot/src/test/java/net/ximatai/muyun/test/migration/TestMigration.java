package net.ximatai.muyun.test.migration;

import io.quarkus.runtime.Startup;
import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.migration.AbstractMigration;
import net.ximatai.muyun.migration.MigrateStep;
import net.ximatai.muyun.model.PageResult;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestMigration {
    @Inject
    FooController fooController;

    @Test
    @DisplayName("测试迁移功能")
    void testMigration() {
        FooController.initData.forEach((item) -> {
            Map<String, Object> view = fooController.view((String) item.get("id"));
            Assertions.assertEquals(item.get("v_name"), view.get("v_name"));
            Assertions.assertEquals((Integer) item.get("i_age") + 10, view.get("i_age"));
        });

        Map<String, Object> view = fooController.view("4");
        Assertions.assertEquals("赵六", view.get("v_name"));
        Assertions.assertEquals(30, view.get("i_age"));
    }

    @Test
    @DisplayName("测试迁移版本校验")
    void testMigrationVersion() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MigrateStep(0, () -> {
            });
        });
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            new MigrateStep(-100, () -> {
            });
        });
        Assertions.assertDoesNotThrow(() -> {
            new MigrateStep(100, () -> {
            });
        });
    }
}

@Startup
class FooMigrate extends AbstractMigration {
    @Inject
    FooController fooController;

    @Override
    public List<MigrateStep> getMigrateSteps() {
        return List.of(
            // 顺序不对不是好的编码习惯，仅用于测试迁移功能
            new MigrateStep(2, () -> {
                fooController.create(Map.of(
                    "id", "4",
                    "v_name", "赵六",
                    "i_age", 30
                ));
            }),
            new MigrateStep(1, () -> {
                PageResult pages = fooController.view(null, null, true, null);
                pages.getList().forEach((item) -> {
                    Map map = (Map) item;
                    Map newMap = new HashMap((Map) item);
                    newMap.remove("id");
                    newMap.put("i_age", (Integer) map.get("i_age") + 10);
                    fooController.update((String) map.get("id"), newMap);
                });
            })
        );
    }

    @Override
    public String getAlias() {
        return "foo";
    }
}

@ApplicationScoped
class FooController extends BaseScaffold {
    public static List<Map> initData = List.of(
        Map.of("id", "1", "v_name", "张三", "i_age", 18),
        Map.of("id", "2", "v_name", "李四", "i_age", 20),
        Map.of("id", "3", "v_name", "王五", "i_age", 22)
    );

    @Override
    public String getMainTable() {
        return "testmigration";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn("v_name")
            .addColumn("i_age");
    }

    @Override
    protected void afterInit() {
        this.batchCreate(FooController.initData);
    }
}
