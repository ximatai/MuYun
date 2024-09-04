package net.ximatai.muyun.model;

public class OrderColumn {

    public static final OrderColumn T_CREATE = new OrderColumn("t_create", Type.DESC);
    public static final OrderColumn I_ORDER = new OrderColumn("i_order");

    private final String columnName;
    private final Type type;

    public enum Type {
        DESC, ASC;

        public boolean isASC() {
            return this == ASC;
        }

    }

    public OrderColumn(String columnName, Type type) {
        this.columnName = columnName;
        this.type = type;
    }

    public OrderColumn(String columnName, String typeString) {
        this.columnName = columnName;
        if (typeString != null) {
            this.type = Type.valueOf(typeString.toUpperCase());
        } else {
            this.type = Type.ASC;
        }
    }

    public OrderColumn(String columnName) {
        this.columnName = columnName;
        this.type = Type.ASC;
    }

    public String getColumnName() {
        return columnName;
    }

    public Type getType() {
        return type;
    }

}
