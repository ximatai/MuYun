package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IChildAbility;

public class ChildTableInfo {
    private IChildAbility ctrl;
    private String foreignKey;
    private boolean isAutoDelete = false;

    public ChildTableInfo(IChildAbility ctrl, String foreignKey) {
        this.ctrl = ctrl;
        this.foreignKey = foreignKey;
    }

    public ChildTableInfo setAutoDelete() {
        this.isAutoDelete = true;
        return this;
    }

    public IChildAbility getCtrl() {
        return ctrl;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public boolean isAutoDelete() {
        return isAutoDelete;
    }
}
