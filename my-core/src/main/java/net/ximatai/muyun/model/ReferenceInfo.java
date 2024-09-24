package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.database.builder.TableBase;

import java.util.HashMap;
import java.util.Map;

public class ReferenceInfo {

    private final String relationColumn;
    private final IReferableAbility ctrl;

    private Map<String, String> translates = new HashMap<>();
    private Map<String, String> otherConditions = new HashMap<>();

    public ReferenceInfo(String relationColumn, IReferableAbility ctrl) {
        this.relationColumn = relationColumn;
        this.ctrl = ctrl;
    }

    public TableBase getReferenceTable() {
        return ctrl.getTableBase();
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
        return column + "_at_" + getReferenceTable().getName();
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

    public ReferenceInfo addOtherCondition(String column, String condition) {
        otherConditions.put(column, condition);
        return this;
    }

    public Map<String, String> getOtherConditions() {
        return otherConditions;
    }
}
