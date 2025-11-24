package net.ximatai.muyun.core.db;

import net.ximatai.muyun.database.core.builder.Column;
import net.ximatai.muyun.database.core.builder.ColumnType;

public class PresetColumn {
    public static final Column ID_POSTGRES_BEFORE_18 = new Column("id")
        .setPrimaryKey()
        .setType(ColumnType.VARCHAR)
        .setDefaultValueAny("gen_random_uuid()");

    public static final Column ID_POSTGRES = new Column("id")
        .setPrimaryKey()
        .setType(ColumnType.VARCHAR)
        .setDefaultValueAny("uuidv7()");

    public static final Column DELETE_FLAG = new Column("b_delete")
        .setType(ColumnType.BOOLEAN)
        .setDefaultValue(false);

    public static final Column TREE_PID = new Column("pid")
        .setType(ColumnType.VARCHAR)
        .setIndexed();

    public static final Column ORDER = new Column("n_order")
        .setSequence()
        .setIndexed();

    public static final Column CODE = new Column("v_code")
        .setType(ColumnType.VARCHAR)
        .setIndexed();

    public static final Column CREATE = new Column("t_create")
        .setIndexed();
}
