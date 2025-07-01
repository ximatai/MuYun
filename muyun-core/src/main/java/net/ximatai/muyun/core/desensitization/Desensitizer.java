package net.ximatai.muyun.core.desensitization;

import java.util.HashMap;
import java.util.Map;

public class Desensitizer {
    private final Map<String, IDesensitizationAlgorithm> columnAlgorithmMap = new HashMap<>();

    public Map<String, IDesensitizationAlgorithm> getAlgorithms() {
        return columnAlgorithmMap;
    }

    /**
     * 注册列名及对应的脱敏算法
     *
     * @param columnName 列名
     * @param algorithm  脱敏算法
     */
    public Desensitizer registerAlgorithm(String columnName, IDesensitizationAlgorithm algorithm) {
        columnAlgorithmMap.put(columnName, algorithm);
        return this;
    }

}
