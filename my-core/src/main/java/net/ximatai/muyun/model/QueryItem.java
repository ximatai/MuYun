package net.ximatai.muyun.model;

public class QueryItem {

    private String field;
    private String label;
    private String alias;

    private SymbolType symbolType;

    private boolean isMain = true; //ui
    private boolean isReference = false;
    private boolean isDate = false;
    private boolean isTime = false;
    private boolean isDatetime = false;
    private boolean isBoolean = false;
    private boolean isHide = false; //ui
    private boolean isNotNull = false;

    private Object defaultValue; //ui

    public QueryItem of(String field) {
        QueryItem item = new QueryItem();
        item.field = field;
        item.alias = field;
        item.symbolType = SymbolType.EQUAL;

        if (field.startsWith("t_")) {
            item.isDatetime = true;
        } else if (field.startsWith("d_")) {
            item.isDate = true;
        } else if (field.startsWith("b_")) {
            item.isBoolean = true;
        }

        return item;
    }

    public enum SymbolType {
        EQUAL, NOT_EQUAL, LIKE, IN, NOT_IN, RANGE
    }

    public QueryItem setField(String field) {
        this.field = field;
        return this;
    }

    public QueryItem setLabel(String label) {
        this.label = label;
        return this;
    }

    public QueryItem setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public QueryItem setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
        return this;
    }

    public QueryItem setMain(boolean main) {
        isMain = main;
        return this;
    }

    public QueryItem setReference(boolean reference) {
        isReference = reference;
        return this;
    }

    public QueryItem setDate(boolean date) {
        isDate = date;
        return this;
    }

    public QueryItem setTime(boolean time) {
        isTime = time;
        return this;
    }

    public QueryItem setDatetime(boolean datetime) {
        isDatetime = datetime;
        return this;
    }

    public QueryItem setBoolean(boolean aBoolean) {
        isBoolean = aBoolean;
        return this;
    }

    public QueryItem setHide(boolean hide) {
        isHide = hide;
        return this;
    }

    public QueryItem setNotNull(boolean notNull) {
        isNotNull = notNull;
        return this;
    }

    public QueryItem setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public String getField() {
        return field;
    }

    public String getLabel() {
        return label;
    }

    public String getAlias() {
        return alias;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public boolean isMain() {
        return isMain;
    }

    public boolean isReference() {
        return isReference;
    }

    public boolean isDate() {
        return isDate;
    }

    public boolean isTime() {
        return isTime;
    }

    public boolean isDatetime() {
        return isDatetime;
    }

    public boolean isBoolean() {
        return isBoolean;
    }

    public boolean isHide() {
        return isHide;
    }

    public boolean isNotNull() {
        return isNotNull;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
