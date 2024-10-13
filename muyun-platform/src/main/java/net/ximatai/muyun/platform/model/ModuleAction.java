package net.ximatai.muyun.platform.model;

public class ModuleAction {

    public static final ModuleAction MENU = new ModuleAction("menu", "菜单").setOrder(0);
    public static final ModuleAction VIEW = new ModuleAction("view", "浏览").setOrder(10);
    public static final ModuleAction EXPORT = new ModuleAction("export", "导出").setOrder(15);
    public static final ModuleAction CREATE = new ModuleAction("create", "新增").setOrder(20);
    public static final ModuleAction IMPORT = new ModuleAction("import", "导入").setOrder(25);
    public static final ModuleAction SORT = new ModuleAction("sort", "排序").setOrder(29);
    public static final ModuleAction UPDATE = new ModuleAction("update", "修改").setOrder(30);
    public static final ModuleAction DELETE = new ModuleAction("delete", "删除").setOrder(40);

    private int order = 0;
    private final String alias;
    private final String name;

    public ModuleAction(String alias, String name) {
        this.alias = alias;
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public ModuleAction setOrder(int order) {
        this.order = order;
        return this;
    }
}
