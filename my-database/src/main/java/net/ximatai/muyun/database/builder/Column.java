package net.ximatai.muyun.database.builder;

public class Column {
    String name;
    String comment;
    String type;
    Object defaultValue;
    boolean nullable;
    boolean unique;
    boolean primaryKey;
    boolean sequence;
    boolean indexed;

    public Column(String name) {
        this.name = name;
    }

    static Column of(String name) {
        return new Column(name);
    }

    Column setComment(String comment) {
        this.comment = comment;
        return this;
    }

    Column setType(String type) {
        this.type = type;
        return this;
    }

    Column setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    Column setNullable(boolean nullable) {
        this.nullable = nullable;
        return this;
    }

    Column setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    Column setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
        return this;
    }

    Column setSequence(boolean sequence) {
        this.sequence = sequence;
        return this;
    }

    Column setIndexed(boolean indexed) {
        this.indexed = indexed;
        return this;
    }

    Column setNullable() {
        this.nullable = true;
        return this;
    }

    Column setUnique() {
        this.unique = true;
        return this;
    }

    Column setPrimaryKey() {
        this.primaryKey = true;
        return this;
    }

    Column setSequence() {
        this.sequence = true;
        return this;
    }

    Column setIndexed() {
        this.indexed = true;
        return this;
    }

}
