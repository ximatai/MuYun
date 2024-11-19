package net.ximatai.muyun.model;

import java.util.HashMap;
import java.util.Map;

public class CheckConfig {

    private final Map<String, String> nonEmptyMap = new HashMap<>();
    private final Map<String, String> uniqueMap = new HashMap<>();

    public void addNonEmpty(String column, String tipWhenEmpty) {
        nonEmptyMap.put(column, tipWhenEmpty);
    }

    public void addUnique(String column, String tipWhenNonUnique) {
        uniqueMap.put(column, tipWhenNonUnique);
    }

    public Map<String, String> getNonEmptyMap() {
        return nonEmptyMap;
    }

    public Map<String, String> getUniqueMap() {
        return uniqueMap;
    }

}
