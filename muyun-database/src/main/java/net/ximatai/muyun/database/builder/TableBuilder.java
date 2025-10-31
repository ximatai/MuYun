package net.ximatai.muyun.database.builder;

import net.ximatai.muyun.database.core.IDatabaseOperations;
import net.ximatai.muyun.database.exception.MyDatabaseException;
import net.ximatai.muyun.database.metadata.DBColumn;
import net.ximatai.muyun.database.metadata.DBIndex;
import net.ximatai.muyun.database.metadata.DBInfo;
import net.ximatai.muyun.database.metadata.DBSchema;
import net.ximatai.muyun.database.metadata.DBTable;

import java.util.*;

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

        List<TableBase> inherits = wrapper.getInherits();

        if (inherits != null && !inherits.isEmpty()) {
            inherits.forEach(inherit -> {
                if (!info.getSchema(inherit.getSchema()).containsTable(inherit.getName())) {
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

        dbTable.resetIndexes();

        return result;

    }

    private boolean checkAndBuildColumn(DBTable dbTable, Column column) {
        boolean result = false;
        String name = column.getName();
        ColumnType dataType = column.getType();
        String type = getColumnTypeTransform().transform(dataType);
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
            if ("varchar".equalsIgnoreCase(type) && defaultValue instanceof String value && !value.contains("(") && !value.contains(")")) {
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

        boolean isColumnTypeChange = !(
            ("bool".equals(dbColumn.getType()) && ColumnType.BOOLEAN.equals(dataType)) ||
                ("_varchar".equals(dbColumn.getType()) && ColumnType.VARCHAR_ARRAY.equals(dataType)) ||
                dbColumn.getType().contains(type.toLowerCase())
        );
        if (isColumnTypeChange) {
            Map<ColumnType, String> typeConverterMap = new HashMap<>();
            typeConverterMap.put(ColumnType.VARCHAR, "");
            typeConverterMap.put(ColumnType.INT, "USING %s::integer");
            typeConverterMap.put(ColumnType.BOOLEAN, "USING %s::boolean");
            typeConverterMap.put(ColumnType.TIMESTAMP, "USING %s::timestamp without time zone");
            typeConverterMap.put(ColumnType.DATE, "USING %s::date");
            typeConverterMap.put(ColumnType.NUMERIC, "USING %s::numeric");
            typeConverterMap.put(ColumnType.JSON, "USING %s::jsonb");
            typeConverterMap.put(ColumnType.VARCHAR_ARRAY, "using %s::integer[]");
            db.execute("alter table %s.%s alter column %s type %s %s;"
                .formatted(dbTable.getSchema(), dbTable.getName(), name, type, typeConverterMap.get(dataType).formatted(name)));
        }

        if (primaryKey && !dbColumn.isPrimaryKey()) {
            db.execute("""
                alter table %s.%s add primary key (%s)
                """.formatted(dbTable.getSchema(), dbTable.getName(), name));
        }

        return result;

    }

    private boolean checkAndBuildIndex(DBTable dbTable, Index index) {
        List<String> columns = index.getColumns();
        List<DBIndex> indexList = dbTable.getIndexList();
        Optional<DBIndex> dbIndexOptional = indexList.stream().filter(i -> new HashSet<>(i.getColumns()).equals(new HashSet<>(columns))).findFirst();

        if (dbIndexOptional.isPresent()) {
            DBIndex dbIndex = dbIndexOptional.get();
            if (dbIndex.isUnique() == index.isUnique()) {
                return false;
            } else {
                db.execute("drop index %s.%s;".formatted(dbTable.getSchema(), dbIndex.getName()));
            }

        }

        String indexName = "%s_%s_".formatted(dbTable.getName(), String.join("_", columns));
        String unique = "";
        String nameSuffix = "index";
        if (index.isUnique()) {
            unique = "unique";
            nameSuffix = "uindex";
        }
        db.execute("create %s index if not exists %s%s on %s.%s(%s);"
            .formatted(unique, indexName, nameSuffix, dbTable.getSchema(), dbTable.getName(), String.join(",", columns)));

        return true;
    }

    private String inheritSQL(List<TableBase> inherits) {
        if (inherits == null || inherits.isEmpty()) {
            return "";
        }

        StringBuilder builder = new StringBuilder("inherits (");
        inherits.forEach(inherit -> {
            builder.append("%s.%s".formatted(inherit.getSchema(), inherit.getName()));
        });
        builder.append(")");
        return builder.toString();
    }

    private IColumnTypeTransform getColumnTypeTransform() {
        String dbName = info.getName().toUpperCase();
        switch (dbName) {
            case "POSTGRESQL":
                return IColumnTypeTransform.POSTGRES;
            default:
                return null;
        }
    }

}
