package net.ximatai.muyun.database.metadata;

public class DBColumn {
    private String name;
    private String description;
    private String type;
    private String defaultValue;
    private String indexName;
    private boolean nullable;
    private boolean primaryKey;
    private boolean unique;
    private boolean indexed;
    private boolean sequence;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public boolean isSequence() {
        return sequence;
    }

    public void setSequence(boolean sequence) {
        this.sequence = sequence;
    }

    public void setSequence() {
        this.sequence = true;
    }

    public void setUnique() {
        this.unique = true;
    }

    public void setNullable() {
        this.nullable = true;
    }

    public void setPrimaryKey() {
        this.primaryKey = true;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public String getIndexName() {
        return indexName;
    }

    public void setIndexName(String indexName) {
        this.indexName = indexName;
    }

}
