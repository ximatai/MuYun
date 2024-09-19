package net.ximatai.muyun.database.builder;

import net.ximatai.muyun.database.IDatabaseOperations;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBInfo;
import net.ximatai.muyun.database.metadata.DBSchema;
import net.ximatai.muyun.database.metadata.DBTable;

import java.util.List;
import java.util.Objects;

public class TableBuilder {

    DBInfo info;
    IDatabaseOperations db;

    public TableBuilder(IDatabaseOperations db) {
        this.db = db;
        this.info = db.getDBInfo();
    }

    public boolean build(TableWrapper wrapper) {
        var result = false;
        String schema = wrapper.getSchema();
        String name = wrapper.getName();

        if (info.getSchema(schema) == null) {
            db.execute("create schema if not exists " + wrapper.getSchema());
            info.addSchema(new DBSchema(wrapper.getSchema()));
        }

        List<String> inherits = wrapper.getInherits();

        if (inherits != null && !inherits.isEmpty()) {
            inherits.forEach(inherit -> {
                if (!info.containsTable(inherit)) {
                    throw new MyDatabaseException("Table " + inherit + " does not exist");
                }
            });
        }

        if (!info.getSchema(wrapper.getSchema()).containsTable(wrapper.getName())) {
            db.execute("""
                create table %s.%s
                (
                    a_temp_column int
                ) %s ;
                """.formatted(schema, name, inheritSQL(inherits)));

            result = true;
            info.getSchema(schema).addTable(new DBTable(db.getJdbi()).setName(name).setSchema(schema));
        }

        DBTable dbTable = info.getSchema(schema).getTable(wrapper.getName());

        if (wrapper.getComment() != null) {
            db.execute("COMMENT ON table %s.%s is '%s'".formatted(schema, name, wrapper.getComment()));
        }

        if (wrapper.getPrimaryKey() != null) {
            checkAndBuildColumn(dbTable, wrapper.getPrimaryKey());
            dbTable.resetColumns();
        }

        wrapper.getColumns().forEach(column -> {
            checkAndBuildColumn(dbTable, column);
        });

        if (result) {
            db.execute("alter table %s.%s drop column a_temp_column;".formatted(schema, name));
        }

        dbTable.resetColumns();

        wrapper.getIndexes().forEach(index -> {
            checkAndBuildIndex(dbTable, index);
        });

        dbTable.resetColumns();

        return result;

    }

    private boolean checkAndBuildColumn(DBTable dbTable, Column column) {
        boolean result = false;
        String name = column.getName();
        String type = column.getType();
        Object defaultValue = column.getDefaultValue();
        String comment = column.getComment();
        boolean sequence = column.isSequence();
        boolean nullable = column.isNullable();
        boolean primaryKey = column.isPrimaryKey();

        if (!dbTable.contains(name)) {
            db.execute("alter table %s.%s add %s %s".formatted(dbTable.getSchema(), dbTable.getName(), name, type));
            dbTable.resetColumns();
            result = true;
        }

        DBColumn dbColumn = dbTable.getColumn(name);

        if (!dbColumn.isSequence() && !Objects.equals(dbColumn.getDefaultValue(), defaultValue)) {
            if ("varchar".equals(type) && defaultValue instanceof String value && !value.contains("(") && !value.contains(")")) {
                defaultValue = "'%s'".formatted(value);
            }

            db.execute("alter table %s.%s alter column %s set default %s".formatted(dbTable.getSchema(), dbTable.getName(), name, defaultValue));
        }

        if (!Objects.equals(dbColumn.getDescription(), comment)) {
            db.execute("comment on column %s.%s.%s is '%s'".formatted(dbTable.getSchema(), dbTable.getName(), name, comment));
        }

        if (dbColumn.isNullable() != nullable) {
            String flag = nullable ? "drop" : "set";
            db.execute("alter table %s.%s alter column %s %s not null".formatted(dbTable.getSchema(), dbTable.getName(), name, flag));
        }

        if (dbColumn.isSequence() != sequence) {
            var seq = "%s_%s_seq".formatted(dbTable.getName(), name);
            if (sequence) {
                db.execute("create sequence if not exists %s.%s;".formatted(dbTable.getSchema(), seq));
                db.execute("alter table %s.%s alter column %s set default nextval('%s.%s')"
                    .formatted(dbTable.getSchema(), dbTable.getName(), name, dbTable.getSchema(), seq));
            } else {
                db.execute("alter table %s.%s alter column %s drop default".formatted(dbTable.getSchema(), dbTable.getName(), name));
                db.execute("drop sequence if exists %s.%s;".formatted(dbTable.getSchema(), seq));
            }
        }

        if (primaryKey && !dbColumn.isPrimaryKey()) {
            db.execute("""
                alter table %s.%s add primary key (%s)
                """.formatted(dbTable.getSchema(), dbTable.getName(), name));
        }

        return result;

    }

    private boolean checkAndBuildIndex(DBTable dbTable, Index index) {
        boolean result = false;
        List<String> columns = index.getColumns();
        int size = columns.size();
        if (size == 1) {
            String col = columns.getFirst();
            DBColumn dbColumn = dbTable.getColumn(col);
            if (index.isUnique() && !dbColumn.isUnique()) { //之前不是唯一索引
                String indexName = "%s_%s_uindex".formatted(dbTable.getName(), col);
                db.execute("create unique index if not exists %s on %s.%s(%s);".formatted(indexName, dbTable.getSchema(), dbTable.getName(), col));
                result = true;
            } else if (!index.isUnique() && !dbColumn.isIndexed()) { //之前没有索引
                String indexName = "%s_%s_index".formatted(dbTable.getName(), col);
                db.execute("create index if not exists %s on %s.%s(%s);".formatted(indexName, dbTable.getSchema(), dbTable.getName(), col));
                result = true;
            }
        } else if (size > 0) {
            if (index.isUnique()) {
                String indexName = "%s_%s_uindex".formatted(dbTable.getName(), String.join("_", columns));
                db.execute("create unique index if not exists %s on %s.%s(%s);".formatted(indexName, dbTable.getSchema(), dbTable.getName(), String.join(",", columns)));
                result = true;
            } else {
                String indexName = "%s_%s_index".formatted(dbTable.getName(), String.join("_", columns));
                db.execute("create index if not exists %s on %s.%s(%s);".formatted(indexName, dbTable.getSchema(), dbTable.getName(), String.join(",", columns)));
                result = true;
            }

        }
        return result;
    }

    private String inheritSQL(List<String> inherits) {
        if (inherits == null || inherits.isEmpty()) {
            return "";
        }

        return "inherits " + String.join(",", inherits);
    }

}
