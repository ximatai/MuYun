package net.ximatai.muyun.database.builder;

import java.util.ArrayList;
import java.util.List;

class Index {
    private List<String> columns;
    private boolean unique;

    public Index(String columnName, boolean unique) {
        this.columns = new ArrayList<>();
        this.columns.add(columnName);
        this.unique = unique;
    }

    public Index(List<String> columns, boolean unique) {
        this.columns = columns;
        this.unique = unique;
    }

    public boolean isUnique() {
        return unique;
    }

    public List<String> getColumns() {
        return columns;
    }
}
