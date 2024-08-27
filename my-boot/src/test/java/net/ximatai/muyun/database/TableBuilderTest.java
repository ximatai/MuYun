package net.ximatai.muyun.database;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import net.ximatai.muyun.database.builder.Column;
import net.ximatai.muyun.database.builder.TableBuilder;
import net.ximatai.muyun.database.builder.TableWrapper;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static net.ximatai.muyun.database.builder.Column.ID_POSTGRES;
import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
//@QuarkusTestResource(value = PostgresTestResource.class, restrictToAnnotatedClass = true)
public class TableBuilderTest {

    @Inject
    Jdbi jdbi;

    @Inject
    IDatabaseAccess databaseAccess;

    @BeforeEach
    void setUp() {
        databaseAccess.execute("DROP TABLE IF EXISTS test.test_table");

        databaseAccess.execute("create schema if not exists test");

        databaseAccess.execute("""
            create table test.%s
            (
                id       varchar   default gen_random_uuid() not null
                    constraint test_table_pk
                        primary key,
                name     varchar,
                t_create timestamp default now()
            )
            """.formatted("test_table"));

        databaseAccess.execute("""
            comment on column test.test_table.name is '名称';
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
        TableWrapper wrapper = new TableWrapper("test_table2")
            .setSchema("test")
            .setComment("a demo")
            .setPrimaryKey(ID_POSTGRES)
            .addColumn(Column.of("v_test").setType("varchar"));
        tableBuilder.build(wrapper);

    }

    @Test
    void testMetadata() {
        var info = databaseAccess.getDBInfo();
        var schema = info.getSchema("test");
        assertNotNull(schema);
        var table = schema.getTable("test_table");
        assertNotNull(table);
        assertFalse(table.getColumns().isEmpty());
        assertNotNull(
            table.getColumns().stream().filter(dbColumn -> "id".equals(dbColumn.getName())).findFirst().orElse(null)
        );
        var nameColumn = table.getColumns().stream().filter(dbColumn -> "name".equals(dbColumn.getName())).findFirst().orElse(null);
        assertNotNull(nameColumn);

        assertEquals(nameColumn.getDescription(), "名称");

    }

}
