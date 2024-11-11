package net.ximatai.muyun.database.builder;

public class Column {
    private String name;
    private String comment;
    private ColumnType type;
    private Object defaultValue;
    private int length;
    private boolean nullable = true;
    private boolean unique = false;
    private boolean primaryKey = false;
    private boolean sequence = false;
    private boolean indexed = false;

    public static final Column ID_POSTGRES = new Column("id")
        .setPrimaryKey()
        .setType(ColumnType.VARCHAR)
        .setDefaultValue("gen_random_uuid()");

    public static final Column DELETE_FLAG = new Column("b_delete")
        .setType(ColumnType.BOOLEAN)
        .setDefaultValue(false);

    public static final Column TREE_PID = new Column("pid")
        .setType(ColumnType.VARCHAR)
        .setIndexed();

    public static final Column ORDER = new Column("n_order")
        .setSequence()
        .setIndexed();

    public static final Column CREATE = new Column("t_create")
        .setIndexed();

    private Column(String name) {
        this.name = name;
        this.type = buildTypeWithColumnName(name);
    }

    public static Column of(String name) {
        return new Column(name);
    }

    public Column setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Column setType(ColumnType type) {
        this.type = type;
        return this;
    }

    public Column setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public Column setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    public Column setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public Column setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    public Column setSequence(boolean sequence) {
        this.sequence = sequence;
        return this;
    }

    public Column setIndexed(boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    public Column setNullable() {
        this.nullable = true;
        return this;
    }

    public Column setUnique() {
        this.unique = true;
        return this;
    }

    public Column setPrimaryKey() {
        this.primaryKey = true;
        this.nullable = false;
        return this;
    }

    public Column setSequence() {
        this.sequence = true;
        return this;
    }

    public Column setIndexed() {
        this.indexed = true;
        return this;
    }

    public String getName() {
        return name;
    }

    public String getComment() {
        return comment;
    }

    public ColumnType getType() {
        return type;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public boolean isUnique() {
        return unique;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public boolean isSequence() {
        return sequence;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public int getLength() {
        return length;
    }

    public Column setLength(int length) {
        this.length = length;
        return this;
    }

    ColumnType buildTypeWithColumnName(String name) {
        ColumnType type = null;

        if ("id".equals(name)) {
            type = ColumnType.VARCHAR;
        } else if ("pid".equals(name)) {
            type = ColumnType.VARCHAR;
        } else if (name.startsWith("v_")) {
            type = ColumnType.VARCHAR;
        } else if (name.startsWith("i_")) {
            type = ColumnType.INT;
        } else if (name.startsWith("b_")) {
            type = ColumnType.BOOLEAN;
        } else if (name.startsWith("t_")) {
            type = ColumnType.TIMESTAMP;
        } else if (name.startsWith("d_")) {
            type = ColumnType.DATE;
        } else if (name.startsWith("n_")) {
            type = ColumnType.NUMERIC;
        } else if (name.startsWith("id_")) {
            type = ColumnType.VARCHAR;
        } else if (name.startsWith("j_")) {
            type = ColumnType.JSON;
        } else if (name.startsWith("dict_")) {
            type = ColumnType.VARCHAR;
        } else if (name.startsWith("file_")) {
            type = ColumnType.VARCHAR;
        } else if (name.startsWith("files_")) {
            type = ColumnType.VARCHAR_ARRAY;
        } else if (name.startsWith("ids_")) {
            type = ColumnType.VARCHAR_ARRAY;
        } else if (name.startsWith("dicts_")) {
            type = ColumnType.VARCHAR_ARRAY;
        }

        return type;
    }
}
