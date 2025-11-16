package net.ximatai.muyun.test.export;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;
import net.ximatai.muyun.ability.IExportAbility;
import net.ximatai.muyun.adaptor.IExportAdaptor;
import net.ximatai.muyun.adaptor.impl.CSVExportAdaptor;
import net.ximatai.muyun.adaptor.impl.ExcelExportAdaptor;
import net.ximatai.muyun.adaptor.impl.JSONExportAdaptor;
import net.ximatai.muyun.base.BaseScaffold;
import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.model.ExportContext;
import net.ximatai.muyun.model.QueryItem;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class ExportOOMTest {
    @Inject
    FooController fooController;

    @Inject
    ExcelExportAdaptor excelExportAdaptor;

    @Inject
    CSVExportAdaptor csvExportAdaptor;

    @Inject
    JSONExportAdaptor jsonExportAdaptor;

    @BeforeAll
    void setup() {
        // 向数据库里导入 100w 行记录
        for (int i = 0; i < 100; i++) {
            // 创建 1w 的数组
            List<Map> batch = new java.util.ArrayList<>(10_000);
            for (int j = 0; j < 10_000; j++) {
                batch.add(Map.of(
                    "v_name", "name_%d".formatted(i * 10_000 + j),
                    "v_value", "value_%d".formatted(i * 10_000 + j)
                ));
            }
            fooController.batchCreate(batch);
        }
    }

    @Test
    void testExportExcel() {
        doTest(excelExportAdaptor);
    }

    @Test
    void testExportCsv() {
        doTest(csvExportAdaptor);
    }

    @Test
    void testExportJson() {
        doTest(jsonExportAdaptor);
    }

    private void doTest(IExportAdaptor adaptor) {
        List<Map> items = new ArrayList<>(fooController.view(null, null, true, null).getList());

        var context = new ExportContext();
        context.setColumns(fooController.getExportColumns(adaptor.getType()));
        context.setItems(items);
        StreamingOutput export = adaptor.export(context);

        // 写到临时目录文件里，方便我们手动打开查看
        var tempDir = System.getProperty("java.io.tmpdir");
        var tempFile = java.nio.file.Path.of(tempDir, "export_" + adaptor.getType() + "_" + System.currentTimeMillis() + adaptor.getFileExtension());

        try (var os = Files.newOutputStream(tempFile)) {
            export.write(os);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

@Path("/foo")
class FooController extends BaseScaffold implements IExportAbility {
    @Override
    public List<QueryItem> queryItemList() {
        return List.of();
    }

    @Override
    public String getMainTable() {
        return "foo_table";
    }

    @Override
    public void fitOut(TableWrapper wrapper) {
        wrapper
            .setPrimaryKey(PresetColumn.ID_POSTGRES)
            .addColumn("v_name")
            .addColumn("v_value");
    }
}
