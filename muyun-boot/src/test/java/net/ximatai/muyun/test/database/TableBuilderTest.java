package net.ximatai.muyun.test.database;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.database.metadata.DBIndex;
import net.ximatai.muyun.database.metadata.DBSchema;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static net.ximatai.muyun.database.builder.Column.ID_POSTGRES;
import static org.junit.jupiter.api.Assertions.*;

//@Disabled
@QuarkusTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TableBuilderTest {

    @Inject
    Jdbi jdbi;

    @Inject
    IDatabaseOperations db;

    @BeforeAll
    void setUp() {
        db.execute("DROP TABLE IF EXISTS test.test_table_x");

        db.execute("create schema if not exists test");

        db.execute("""
            create table test.%s
            (
                id       varchar   default gen_random_uuid() not null
                    constraint test_table_x_pk
                        primary key,
                name     varchar,
                t_create timestamp default now()
            )
            """.formatted("test_table_x"));

        db.execute("""
            comment on column test.test_table_x.name is '名称';
            """);

        db.resetDBInfo();

        DBSchema test = db.getDBInfo().getSchema("test");
        assertNotNull(test);
    }

    @Test
    @DisplayName("验证数据库连接是否正常")
    void testDB() {
        jdbi.useHandle(h -> {
            var row = h.createQuery("select 1 as title")
                .mapToMap().findOne().orElseThrow(RuntimeException::new);
            assertEquals(1, row.get("title"));
        });
    }

    @Test
    @DisplayName("验证表构建功能是否正确")
    void testTableBuilder() {
        TableBuilder tableBuilder = new TableBuilder(db);
        TableWrapper wrapper = TableWrapper.withName("test_table_x2")
            .setSchema("test")
            .setComment("a demo")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn(Column.of("v_test"))
            .addColumn(Column.of("v_test2"))
            .addColumn(Column.of("b_test").setDefaultValue(true))
            .addIndex("v_test")
            .addIndex("v_test2", true)
            .addIndex(List.of("v_test", "v_test2"));
        tableBuilder.build(wrapper);

        DBSchema schema = db.getDBInfo().getSchema("test");

        assertTrue(schema.containsTable("test_table_x2"));
        assertTrue(schema.getTable("test_table_x2").getColumn("id").isPrimaryKey());
        List<DBIndex> indexList = schema.getTable("test_table_x2").getIndexList();
        Optional<DBIndex> dbIndex = indexList.stream().filter(index -> index.getColumns().contains("v_test")).findFirst();
        Optional<DBIndex> dbIndex2 = indexList.stream().filter(index -> index.getColumns().contains("v_test2") && index.isMulti()).findFirst();

        assertTrue(dbIndex.isPresent());
        assertTrue(dbIndex2.isPresent());
        assertTrue(dbIndex2.get().getColumns().contains("v_test"));
        assertTrue(dbIndex2.get().getColumns().contains("v_test2"));

        Object boolDefaultValue = schema.getTable("test_table_x2").getColumn("b_test").getDefaultValue();
        assertEquals(true, boolDefaultValue);
    }

    @Test
    @DisplayName("验证重复构建表时的行为")
    void testBuildTwice() {
        TableBuilder tableBuilder = new TableBuilder(db);
        TableWrapper wrapper = TableWrapper.withName("test_table_x3")
            .setSchema("test")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn(Column.of("b_test").setDefaultValue(false));
        tableBuilder.build(wrapper);

        assertFalse((Boolean) db.getDBInfo().getSchema("test").getTable("test_table_x3")
            .getColumn("b_test").getDefaultValue());
    }

    @Test
    @DisplayName("验证元数据信息是否正确")
    void testMetadata() {
        var info = db.getDBInfo();
        var schema = info.getSchema("test");
        assertNotNull(schema);
        var table = schema.getTable("test_table_x");
        assertNotNull(table);
        assertFalse(table.getColumnMap().isEmpty());
        assertNotNull(table.getColumnMap().get("id"));
        assertNotNull(table.getColumnMap().get("name"));
        assertEquals(table.getColumnMap().get("name").getDescription(), "名称");
    }

    @Test
    @DisplayName("验证通过DatabaseMetaData读取索引信息")
    void testMetadataRead() throws SQLException {
        jdbi.useHandle(h -> {
            Connection conn = h.getConnection();
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getIndexInfo(null, "test", "test_table_x2", false, false)) {
                while (rs.next()) {
                    System.out.println(rs.getString("COLUMN_NAME"));
                    System.out.println(rs.getString("INDEX_NAME"));
                }
            }
        });
    }

    @Test
    @DisplayName("测试继承表的功能")
    void testInherits() {
        TableWrapper basic = TableWrapper.withName("basic")
            .setSchema("public")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn("v_name");

        new TableBuilder(db).build(basic);

        TableWrapper child = TableWrapper.withName("child")
            .setSchema("public")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn("v_test")
            .setInherits(List.of(basic));

        new TableBuilder(db).build(child);

        String id = (String) db.insertItem("public", "child", Map.of("v_test", "test", "v_name", "name"));

        assertNotNull(id);

        Map childRow = (Map) db.row("select * from public.child where id = ?", id);
        assertEquals("test", childRow.get("v_test"));
        assertEquals("name", childRow.get("v_name"));

        Map mainRow = (Map) db.row("select * from public.basic where id = ?", id);
        assertNull(mainRow.get("v_test"));
        assertEquals("name", mainRow.get("v_name"));

        DBSchema dbSchema = db.getDBInfo().getSchema("public");
        assertTrue(dbSchema.getTable("basic").getColumn("id").isPrimaryKey());
        assertTrue(dbSchema.getTable("child").getColumn("id").isPrimaryKey());
    }
}
