package net.ximatai.muyun.model;

import org.eclipse.microprofile.openapi.annotations.media.Schema;

@Schema(description = "可以被查询的列")
public class QueryItem {

    @Schema(description = "列名")
    private String column;
    @Schema(description = "标题")
    private String label;
    @Schema(description = "别名")
    private String alias;
    @Schema(description = "比较类型")
    private SymbolType symbolType;

    private boolean isMain = true; //ui
    private boolean isReference = false;
    private boolean isDate = false;
    private boolean isTime = false;
    private boolean isDatetime = false;
    private boolean isBoolean = false;
    private boolean isHide = false; //ui
    private boolean isRequired = false;
    private boolean isNullQuery = false;
    private boolean isStringBlankQuery = false;

    private Object defaultValue;

    private QueryItem() {
    }

    public static QueryItem of(String column) {
        QueryItem item = new QueryItem();
        item.column = column;
        item.alias = column;
        item.symbolType = SymbolType.EQUAL;

        if (column.startsWith("t_")) {
            item.isDatetime = true;
        } else if (column.startsWith("d_")) {
            item.isDate = true;
        } else if (column.startsWith("b_")) {
            item.isBoolean = true;
        }

        return item;
    }

    @Schema(description = "比较类型")
    public enum SymbolType {
        EQUAL, NOT_EQUAL, LIKE, IN, NOT_IN, RANGE,
        PG_ARRAY_EQUAL, PG_ARRAY_OVERLAP, PG_ARRAY_CONTAIN, PG_ARRAY_BE_CONTAIN
    }

    public QueryItem setColumn(String column) {
        this.column = column;
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

    public QueryItem setRequired(boolean required) {
        isRequired = required;
        return this;
    }

    public QueryItem setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
        return this;
    }

    public QueryGroup toGroup() {
        return QueryGroup.of(this);
    }

    public String getColumn() {
        return column;
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

    public boolean isRequired() {
        return isRequired;
    }

    /**
     * @return null 值是否参与查询
     */
    public boolean isNullQuery() {
        return isNullQuery;
    }

    public QueryItem setNullQuery(boolean nullQuery) {
        isNullQuery = nullQuery;
        return this;
    }

    public boolean isStringBlankQuery() {
        return isStringBlankQuery;
    }

    public QueryItem setStringBlankQuery(boolean stringBlankQuery) {
        isStringBlankQuery = stringBlankQuery;
        return this;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
