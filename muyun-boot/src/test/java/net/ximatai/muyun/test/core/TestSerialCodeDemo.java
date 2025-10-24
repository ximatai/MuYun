package net.ximatai.muyun.test.core;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.ximatai.muyun.ability.ICodeGenerateAbility;
import net.ximatai.muyun.ability.ITableCreateAbility;
import net.ximatai.muyun.ability.curd.std.ICURDAbility;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.core.Scaffold;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.code.CodeGenerateConfig;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@QuarkusTest
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestSerialCodeDemo {

    @Inject
    SerialCodeDemo serialCodeDemo;

    @Test
    @DisplayName("测试批量创建和单个创建序列码")
    void test() {
        List<String> ids = serialCodeDemo.batchCreate(
            List.of(
                Map.of("v_name", "test"),
                Map.of("v_name", "test"),
                Map.of("v_name", "test"),
                Map.of("v_name", "test"),
                Map.of("v_name", "test")
            )
        );

        Map<String, ?> map = serialCodeDemo.view(ids.getLast());

        String yyyyMMdd = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

        Assertions.assertEquals("BSY%s0005".formatted(yyyyMMdd), map.get("v_code"));

        String idNew = serialCodeDemo.create(Map.of("v_name", "test"));

        Map<String, ?> view = serialCodeDemo.view(idNew);
        Assertions.assertEquals("BSY%s0006".formatted(yyyyMMdd), view.get("v_code"));

        String id2 = serialCodeDemo.create(Map.of("v_name", "test", "v_code", "my_code"));
        Map<String, ?> view2 = serialCodeDemo.view(id2);
        Assertions.assertEquals("my_code", view2.get("v_code"));

        String id3 = serialCodeDemo.create(Map.of("v_name", "test"));
        Map<String, ?> view3 = serialCodeDemo.view(id3);
        Assertions.assertEquals("BSY%s0007".formatted(yyyyMMdd), view3.get("v_code"));
    }

}

@ApplicationScoped
class SerialCodeDemo extends Scaffold implements ITableCreateAbility, ICodeGenerateAbility, ICURDAbility {

    @Override
    public CodeGenerateConfig getCodeGenerateConfig() {
        return new CodeGenerateConfig("BSY", true, 4);
    }

    @Override
    public String getMainTable() {
        return "test_code";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn("v_code")
            .addColumn("v_name");
    }
}
