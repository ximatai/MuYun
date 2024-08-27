package net.ximatai.muyun.database.builder;

import jakarta.transaction.Transactional;
import net.ximatai.muyun.database.IDatabaseAccess;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBInfo;
import net.ximatai.muyun.database.metadata.DBSchema;
import net.ximatai.muyun.database.metadata.DBTable;

import java.util.List;
import java.util.Objects;

public class TableBuilder {

    DBInfo info;
    IDatabaseAccess databaseAccess;

    public TableBuilder(IDatabaseAccess databaseAccess) {
        this.databaseAccess = databaseAccess;
        this.info = databaseAccess.getDBInfo();
    }

    @Transactional
    public boolean build(TableWrapper wrapper) {
        var result = false;
        String schema = wrapper.getSchema();
        String name = wrapper.getName();

        if (info.getSchema(schema) == null) {
            databaseAccess.execute("create schema if not exists " + wrapper.getSchema());
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
            databaseAccess.execute("""
                create table %s.%s
                (
                    a_temp_column int
                ) %s ;
                """.formatted(schema, name, inheritSQL(inherits)));

            result = true;
            info.getSchema(schema).addTable(new DBTable(databaseAccess.getJdbi()).setName(name).setSchema(schema));
        }

        DBTable dbTable = info.getSchema(schema).getTable(wrapper.getName());

        if (wrapper.getComment() != null) {
            databaseAccess.execute("COMMENT ON table %s.%s is '%s'".formatted(schema, name, wrapper.getComment()));
        }

        if (wrapper.getPrimaryKey() != null) {
            checkAndBuildColumn(dbTable, wrapper.getPrimaryKey());
            dbTable.resetColumns();
        }

        wrapper.getColumns().forEach(column -> {
            checkAndBuildColumn(dbTable, column);
        });

        if (result) {
            databaseAccess.execute("alter table %s.%s drop column a_temp_column;".formatted(schema, name));
        }

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
            databaseAccess.execute("alter table %s.%s add %s %s".formatted(dbTable.getSchema(), dbTable.getName(), name, type));
            dbTable.resetColumns();
            result = true;
        }

        DBColumn dbColumn = dbTable.getColumn(name);

        if (!Objects.equals(dbColumn.getDefaultValue(), defaultValue)) {
            databaseAccess.execute("alter table %s.%s alter column %s set default %s".formatted(dbTable.getSchema(), dbTable.getName(), name, defaultValue));
        }

        if (!Objects.equals(dbColumn.getDescription(), comment)) {
            databaseAccess.execute("comment on column %s.%s.%s is '%s'".formatted(dbTable.getSchema(), dbTable.getName(), name, comment));
        }

        if (dbColumn.isNullable() != nullable) {
            String flag = nullable ? "drop" : "set";
            databaseAccess.execute("alter table %s.%s alter column %s %s not null".formatted(dbTable.getSchema(), dbTable.getName(), name, flag));
        }

        if (dbColumn.isSequence() != sequence) {
            var seq = "%s_%s_seq".formatted(dbTable.getName(), name);
            if (sequence) {
                databaseAccess.execute("create sequence if not exists %s.%s;".formatted(dbTable.getSchema(), seq));
                databaseAccess.execute("alter table %s.%s alter column %s set default nextval('%s.%s')"
                    .formatted(dbTable.getSchema(), dbTable.getName(), name, dbTable.getSchema(), seq));
            } else {
                databaseAccess.execute("alter table %s.%s alter column %s drop default".formatted(dbTable.getSchema(), dbTable.getName(), name));
                databaseAccess.execute("drop sequence if exists %s.%s;".formatted(dbTable.getSchema(), seq));
            }
        }

        if (primaryKey && !dbColumn.isPrimaryKey()) {
            databaseAccess.execute("""
                alter table %s.%s add primary key (%s)
                """.formatted(dbTable.getSchema(), dbTable.getName(), name));
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
