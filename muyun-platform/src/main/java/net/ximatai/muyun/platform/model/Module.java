package net.ximatai.muyun.platform.model;

import java.util.ArrayList;
import java.util.List;

public class Module {
    private String name;
    private String alias = "void";
    private String url;
    private String table;
    private String remark;
    private boolean bSystem;
    private List<ModuleAction> actions = new ArrayList<>();

    private Module() {
    }

    public static Module ofName(String name) {
        return new Module().setName(name);
    }

    public String getName() {
        return name;
    }

    public Module setName(String name) {
        this.name = name;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public Module setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public Module setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getTable() {
        return table;
    }

    public Module setTable(String table) {
        this.table = table;
        return this;
    }

    public String getRemark() {
        return remark;
    }

    public Module setRemark(String remark) {
        this.remark = remark;
        return this;
    }

    public boolean isbSystem() {
        return bSystem;
    }

    public Module setbSystem(boolean bSystem) {
        this.bSystem = bSystem;
        return this;
    }

    public List<ModuleAction> getActions() {
        return actions;
    }

    public Module addAction(ModuleAction action) {
        this.actions.add(action);
        return this;
    }
}
