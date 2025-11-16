package net.ximatai.muyun.model;

import java.util.function.Function;

/**
 * 导出列配置
 */
public class ExportColumn {

    private String fieldName;        // 数据字段名
    private String displayName;      // 显示名称（列头）
    private Function<Object, String> formatter;  // 值格式化器

    public ExportColumn() {
    }

    public ExportColumn(String fieldName, String displayName) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.formatter = null;
    }

    public ExportColumn(String fieldName, String displayName, Function<Object, String> formatter) {
        this.fieldName = fieldName;
        this.displayName = displayName;
        this.formatter = formatter;
    }

    public static ExportColumn of(String fieldName, String displayName) {
        return new ExportColumn(fieldName, displayName);
    }

    public static ExportColumn of(String fieldName, String displayName, Function<Object, String> formatter) {
        return new ExportColumn(fieldName, displayName, formatter);
    }

    /**
     * 格式化值
     */
    public String format(Object value) {
        if (value == null) {
            return "";
        }
        if (formatter != null) {
            return formatter.apply(value);
        }
        return value.toString();
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public Function<Object, String> getFormatter() {
        return formatter;
    }

    public void setFormatter(Function<Object, String> formatter) {
        this.formatter = formatter;
    }
}

