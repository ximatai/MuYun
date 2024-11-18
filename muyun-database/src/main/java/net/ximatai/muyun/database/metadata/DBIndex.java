package net.ximatai.muyun.database.metadata;

import java.util.ArrayList;
import java.util.List;

public class DBIndex {
    private String name;
    private boolean unique = false;
    private final List<String> columns = new ArrayList<>();

    public String getName() {
        return name;
    }

    public DBIndex setName(String name) {
        this.name = name;
        return this;
    }

    public boolean isUnique() {
        return unique;
    }

    public DBIndex setUnique(boolean unique) {
        this.unique = unique;
        return this;
    }

    public List<String> getColumns() {
        return columns;
    }

    public DBIndex addColumn(String columns) {
        this.columns.add(columns);
        return this;
    }

    public boolean isMulti() {
        return columns.size() > 1;
    }
}
