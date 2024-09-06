package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IReferableAbility;

import java.util.HashMap;
import java.util.Map;

public class ReferenceInfo {

    private final String relationColumn;
    private final IReferableAbility ctrl;

    private Map<String, String> translates = new HashMap<>();

    public ReferenceInfo(String relationColumn, IReferableAbility ctrl) {
        this.relationColumn = relationColumn;
        this.ctrl = ctrl;
    }

    public String getReferenceTable() {
        return ctrl.getMainTable();
    }

    public String getHitField() {
        return ctrl.getKeyColumn();
    }

    public ReferenceInfo add(String column) {
        putTranslate(column);
        return this;
    }

    public ReferenceInfo add(String column, String alias) {
        putTranslate(column, alias);
        return this;
    }

    public ReferenceInfo autoPackage() {
        ctrl.getOpenColumns().forEach(column -> {
            putTranslate(column, getColumnAlias(column));
        });

        putTranslate(ctrl.getLabelColumn());
        return this;
    }

    private String getColumnAlias(String column) {
        return column + "_at_" + getReferenceTable();
    }

    private void putTranslate(String column) {
        String alias = getColumnAlias(column);
        putTranslate(column, alias);
    }

    private void putTranslate(String field, String alias) {
        if (ctrl.checkColumnExist(field)) {
            translates.put(field, alias);
        }
    }

    public String getRelationColumn() {
        return relationColumn;
    }

    public Map<String, String> getTranslates() {
        return translates;
    }
}
