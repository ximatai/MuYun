package net.ximatai.muyun.model;

import net.ximatai.muyun.ability.IReferableAbility;
import net.ximatai.muyun.database.builder.TableBase;

import java.util.HashMap;
import java.util.Map;

public class ReferenceInfo {

    // 关联字段
    private final String relationColumn;
    // 关联的 Controller
    private final IReferableAbility ctrl;
    // 是否深度关联，如果深度关联，就回把原始 ctrl的查询语句作为 被关联的字查询拉出来，这样可以就可以获取该 ctrl 已关联的字段。
    private boolean isDeep = false;

    // 需要翻译带出的字段
    private Map<String, String> translates = new HashMap<>();
    // 除了关联字段外，其他辅助条件，比如关联数据字典，就需要多一个字典类目作为过滤条件
    private Map<String, String> otherConditions = new HashMap<>();

    public ReferenceInfo(String relationColumn, IReferableAbility ctrl) {
        this.relationColumn = relationColumn;
        this.ctrl = ctrl;
    }

    public String getDeepSelectSql() {
        return ctrl.getSelectSql();
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

        if (ctrl.getLabelColumn() != null) {
            putTranslate(ctrl.getLabelColumn());
        }

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
        if (isDeep || ctrl.checkColumnExist(field)) { // 如果是 deep 模式，字段就不用检查了
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

    public boolean isDeep() {
        return isDeep;
    }

    public ReferenceInfo setDeep(boolean deep) {
        isDeep = deep;
        return this;
    }

    public ReferenceInfo setDeep() {
        return this.setDeep(true);
    }
}
