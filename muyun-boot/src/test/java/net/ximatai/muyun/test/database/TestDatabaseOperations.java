package net.ximatai.muyun.test.database;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;
import net.ximatai.muyun.database.core.builder.TableBuilder;
import net.ximatai.muyun.database.core.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.Map;

import static net.ximatai.muyun.core.db.PresetColumn.ID_POSTGRES_UUID_V7;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class)
public class TestDatabaseOperations {

    @Inject
    IDatabaseOperations db;

    @BeforeAll
    void setUp() {
        TableWrapper basic = TableWrapper.withName("basic")
            .setSchema("public")
            .setPrimaryKey(ID_POSTGRES_UUID_V7)
            .addColumn("v_name")
            .addColumn("i_age")
            .addColumn(Column.of("av_test").setType(ColumnType.VARCHAR_ARRAY));

        new TableBuilder(db).build(basic);
    }

    @Test
    void testArray() {
        String id = db.insertItem("public", "basic", Map.of(
            "v_name", "test",
            "i_age", 1,
            "av_test", List.of("a", "b")
        ));

        Map row = db.row("select * from public.basic where id = ?", id);
        assertEquals("test", row.get("v_name"));
        assertEquals(1, row.get("i_age"));
        String[] avTest = (String[]) row.get("av_test");
        assertTrue(avTest.length == 2);
        assertTrue(avTest[0].equals("a"));
        assertTrue(avTest[1].equals("b"));
    }

    @Test
    void testExecute() {
        db.execute("select 'test',?,?", "x", "y");
        db.execute("select 'test',?,?", List.of("x", "y"));
    }

    @Test
    @DisplayName("验证单条记录插入操作")
    void testInsert() {
        String id = db.insertItem("public", "basic", Map.of(
            "v_name", "test",
            "i_age", 1
        ));

        Map row = db.row("select * from public.basic where id = ?", id);
        assertEquals("test", row.get("v_name"));
        assertEquals(1, row.get("i_age"));
    }

    @Test
    @DisplayName("验证批量记录插入操作")
    void testBatchInsert() {
        List list = List.of(
            Map.of(
                "v_name", "test1",
                "i_age", 1
            ),
            Map.of(
                "v_name", "test2",
                "i_age", 2
            ),
            Map.of(
                "v_name", "test3",
                "i_age", 3
            )

        );

        List idList = db.insertList("public", "basic", list);

        Map row = db.row("select * from public.basic where id = ?", idList.get(0));
        assertEquals("test1", row.get("v_name"));
        assertEquals(1, row.get("i_age"));

        Map row2 = db.row("select * from public.basic where id = ?", idList.get(1));
        assertEquals("test2", row2.get("v_name"));
        assertEquals(2, row2.get("i_age"));

        Map row3 = db.row("select * from public.basic where id = ?", idList.get(2));
        assertEquals("test3", row3.get("v_name"));
        assertEquals(3, row3.get("i_age"));
    }
}
