package net.ximatai.muyun.platform.model;

import java.util.HashMap;
import java.util.Map;

public class Dict {
    private String categoryID;
    private String value;
    private String name;
    private int order;

    public Dict(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public Dict setOrder(int order) {
        this.order = order;
        return this;
    }

    public Dict setCategoryID(String categoryID) {
        this.categoryID = categoryID;
        return this;
    }

    public Map<String, Object> toMap() {
        Map map = new HashMap();
        map.put("id_at_app_dictcategory", categoryID);
        map.put("v_value", value);
        map.put("v_name", name);
        map.put("n_order", order);
        return map;
    }

}
