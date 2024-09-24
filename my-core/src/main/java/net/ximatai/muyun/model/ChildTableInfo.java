package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IChildAbility;

public class ChildTableInfo {
    private final IChildAbility ctrl;
    private final String foreignKey;
    private boolean isAutoDelete = false;
    private final String childAlias;

    public ChildTableInfo(String foreignKey, IChildAbility ctrl) {
        this.foreignKey = foreignKey;
        this.ctrl = ctrl;
        this.childAlias = ctrl.getChildAlias();
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

    public String getChildAlias() {
        return childAlias;
    }
}
