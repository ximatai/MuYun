package net.ximatai.muyun.platform.model;

import java.util.ArrayList;
import java.util.List;

public class ModuleConfig {
    private String name;
    private String alias = "void";
    private String url;
    private String table;
    private String remark;
    private boolean bSystem;
    private List<ModuleAction> actions = new ArrayList<>();

    private ModuleConfig() {
    }

    public static ModuleConfig ofName(String name) {
        return new ModuleConfig().setName(name);
    }

    public String getName() {
        return name;
    }

    public ModuleConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public ModuleConfig setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public ModuleConfig setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTable() {
        return table;
    }

    public ModuleConfig setTable(String table) {
        this.table = table;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public ModuleConfig setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public boolean isbSystem() {
        return bSystem;
    }

    public ModuleConfig setbSystem(boolean bSystem) {
        this.bSystem = bSystem;
        return this;
    }

    public List<ModuleAction> getActions() {
        return actions;
    }

    public ModuleConfig addAction(ModuleAction action) {
        this.actions.add(action);
        return this;
    }

    public ModuleConfig addActions(List<ModuleAction> action) {
        this.actions.addAll(action);
        return this;
    }
}
