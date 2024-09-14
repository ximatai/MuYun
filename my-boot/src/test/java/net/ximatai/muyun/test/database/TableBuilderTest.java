package net.ximatai.muyun.test.database;

import io.quarkus.test.common.QuarkusTestResource;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;
import net.ximatai.muyun.test.testcontainers.PostgresTestResource;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

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
    IDatabaseAccess databaseAccess;

    @BeforeAll
    void setUp() {
        databaseAccess.execute("DROP TABLE IF EXISTS test.test_table_x");

        databaseAccess.execute("create schema if not exists test");

        databaseAccess.execute("""
            create table test.%s
            (
                id       varchar   default gen_random_uuid() not null
                    constraint test_table_x_pk
                        primary key,
                name     varchar,
                t_create timestamp default now()
            )
            """.formatted("test_table_x"));

        databaseAccess.execute("""
            comment on column test.test_table_x.name is '名称';
            """);
    }

    @Test
    void testDB() {
        jdbi.useHandle(h -> {
            var row = h.createQuery("select 1 as title")
                .mapToMap().findOne().orElseThrow(RuntimeException::new);
            assertEquals(1, row.get("title"));
        });
    }

    @Test
    void testTableBuilder() {
        TableBuilder tableBuilder = new TableBuilder(databaseAccess);
        TableWrapper wrapper = TableWrapper.withName("test_table_x2")
            .setSchema("test")
            .setComment("a demo")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn(Column.of("v_test").setType("varchar"))
            .addColumn(Column.of("v_test2").setType("varchar"))
            .addIndex("v_test")
            .addIndex("v_test2", true)
            .addIndex(List.of("v_test", "v_test2"));
        tableBuilder.build(wrapper);

        assertTrue(databaseAccess.getDBInfo().getSchema("test").containsTable("test_table_x2"));
        assertTrue(databaseAccess.getDBInfo().getSchema("test").getTable("test_table_x2").getColumn("id").isPrimaryKey());
        assertFalse(databaseAccess.getDBInfo().getSchema("test").getTable("test_table_x2").getColumn("v_test").isUnique());
        assertTrue(databaseAccess.getDBInfo().getSchema("test").getTable("test_table_x2").getColumn("v_test").isIndexed());
        assertTrue(databaseAccess.getDBInfo().getSchema("test").getTable("test_table_x2").getColumn("v_test2").isUnique());
    }

    @Test
    void testMetadata() {
        var info = databaseAccess.getDBInfo();
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
}
