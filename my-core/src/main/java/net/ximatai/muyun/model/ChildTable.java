package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.curd.std.ICURDAbility;

public class ChildTable {
    private ICURDAbility ctrl;
    private String foreignKey;
    private boolean isAutoDelete = false;

    public ChildTable(ICURDAbility ctrl, String foreignKey) {
        this.ctrl = ctrl;
        this.foreignKey = foreignKey;
    }

    public ChildTable setAutoDelete() {
        this.isAutoDelete = true;
        return this;
    }

    public ICURDAbility getCtrl() {
        return ctrl;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public boolean isAutoDelete() {
        return isAutoDelete;
    }
}
