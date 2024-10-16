package net.ximatai.muyun.platform.model;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictCategory {
    private String id;
    private String pid;
    private String name;
    private int order;
    private Dict[] dictList;

    public DictCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public DictCategory(String id, String name, int order) {
        this.id = id;
        this.name = name;
        this.order = order;
    }

    public DictCategory(String id, String pid, String name, int order) {
        this.id = id;
        this.pid = pid;
        this.name = name;
        this.order = order;
    }

    public String getId() {
        return id;
    }

    public String getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public int getOrder() {
        return order;
    }

    public DictCategory setDictList(Dict... dictList) {
        for (int i = 0; i < dictList.length; i++) {
            Dict dict = dictList[i];
            dict.setCategoryID(this.id);
            dict.setOrder(i);
        }

        this.dictList = dictList;
        return this;
    }

    public DictCategory setDictList(List<Dict> dictList) {
        return this.setDictList(dictList.toArray(new Dict[0]));
    }

    public Map toMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("id", this.id);
        map.put("pid", this.pid);
        map.put("v_name", this.name);
        map.put("n_order", this.order);
        if (this.dictList != null) {
            map.put("app_dict", Arrays.stream(this.dictList).map(Dict::toMap).toList());
        }

        return map;
    }
}
