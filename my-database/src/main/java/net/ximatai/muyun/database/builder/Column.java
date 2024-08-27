package net.ximatai.muyun.database.builder;

public class Column {
    private String name;
    private String comment;
    private String type;
    private Object defaultValue;
    private boolean nullable = true;
    private boolean unique = false;
    private boolean primaryKey = false;
    private boolean sequence = false;
    private boolean indexed = false;

    public static Column ID_POSTGRES = new Column("id")
        .setPrimaryKey()
        .setType("varchar")
        .setDefaultValue("gen_random_uuid()");

    private Column(String name) {
        this.name = name;
    }

    public static Column of(String name) {
        return new Column(name);
    }

    public Column setComment(String comment) {
        this.comment = comment;
        return this;
    }

    public Column setType(String type) {
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

    public String getType() {
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
}
