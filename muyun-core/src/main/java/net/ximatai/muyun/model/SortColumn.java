package net.ximatai.muyun.model;

import net.ximatai.muyun.core.db.PresetColumn;
import net.ximatai.muyun.database.core.builder.Column;

public class SortColumn {

    public static final SortColumn CREATE = new SortColumn(PresetColumn.CREATE, Type.DESC);
    public static final SortColumn ORDER = new SortColumn(PresetColumn.ORDER, Type.ASC);
    public static final SortColumn CODE = new SortColumn(PresetColumn.CODE, Type.ASC);

    private Column column;
    private final String columnName;
    private final Type type;

    public enum Type {
        DESC, ASC;

        public boolean isASC() {
            return this == ASC;
        }
    }

    public SortColumn(Column column, Type type) {
        this.column = column;
        this.columnName = column.getName();
        this.type = type;
    }

    public SortColumn(String columnName, Type type) {
        this.columnName = columnName;
        this.type = type;
    }

    public SortColumn(String columnName, String typeString) {
        this.columnName = columnName;
        if (typeString != null) {
            this.type = Type.valueOf(typeString.toUpperCase());
        } else {
            this.type = Type.ASC;
        }
    }

    public SortColumn(String columnName) {
        this.columnName = columnName;
        this.type = Type.ASC;
    }

    public String getColumnName() {
        return columnName;
    }

    public Type getType() {
        return type;
    }

    public Column getColumn() {
        if (column == null) {
            column = Column.of(columnName);
        }
        return column;
    }
}
