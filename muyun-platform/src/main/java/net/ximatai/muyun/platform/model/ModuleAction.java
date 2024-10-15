package net.ximatai.muyun.platform.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleAction {

    public static final ModuleAction MENU = new ModuleAction("menu", "菜单", 0);
    public static final ModuleAction VIEW = new ModuleAction("view", "浏览", 10);
    public static final ModuleAction CREATE = new ModuleAction("create", "新增", 20);
    public static final ModuleAction SORT = new ModuleAction("sort", "排序", 29);
    public static final ModuleAction UPDATE = new ModuleAction("update", "修改", 30);
    public static final ModuleAction DELETE = new ModuleAction("delete", "删除", 40);

    public static final List<ModuleAction> DEFAULT_ACTIONS = List.of(
        MENU, VIEW, CREATE, UPDATE, DELETE
    );

    private int order = 0;
    private final String alias;
    private final String name;

    public enum TypeLike {
        VIEW, UPDATE, CREATE, DELETE
    }

    public ModuleAction(String alias, String name, int order) {
        this.alias = alias;
        this.name = name;
        this.order = order;
    }

    public ModuleAction(String alias, String name, TypeLike typeLike) {
        this.alias = alias;
        this.name = name;
        this.order = switch (typeLike) {
            case VIEW -> 11;
            case UPDATE -> 31;
            case CREATE -> 21;
            case DELETE -> 41;
        };
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

    public Map toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("v_alias", alias);
        map.put("v_name", name);
        map.put("i_order", order);
        return map;
    }
}
