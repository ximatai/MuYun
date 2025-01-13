package net.ximatai.muyun.model;

import java.util.ArrayList;
import java.util.List;

public class QueryGroup {

    private final QueryItem queryItem;
    private List<QueryGroup> andGroups = new ArrayList<>();
    private List<QueryGroup> orGroups = new ArrayList<>();

    private QueryGroup() {
        this.queryItem = null;
    }

    private QueryGroup(QueryItem column) {
        this.queryItem = column;
    }

    public static QueryGroup of(List<QueryItem> columns) {
        if (columns == null || columns.isEmpty()) {
            return null;
        }

        QueryGroup first = null;
        for (int i = 0; i < columns.size(); i++) {
            QueryGroup group = new QueryGroup(columns.get(i));
            if (i == 0) {
                first = group;
            } else {
                first.and(group);
            }
        }

        return first;
    }

    public static QueryGroup ofBlank() {
        return new QueryGroup();
    }

    public static QueryGroup of(QueryItem column) {
        return new QueryGroup(column);
    }

    public QueryGroup and(QueryGroup group) {
        this.andGroups.add(group);
        return this;
    }

    public QueryGroup or(QueryGroup group) {
        this.orGroups.add(group);
        return this;
    }

    public QueryItem getQueryItem() {
        return queryItem;
    }

    public List<QueryGroup> getAndGroups() {
        return andGroups;
    }

    public List<QueryGroup> getOrGroups() {
        return orGroups;
    }

}
