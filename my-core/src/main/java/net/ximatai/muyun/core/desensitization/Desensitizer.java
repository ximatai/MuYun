package net.ximatai.muyun.core.desensitization;

import java.util.HashMap;
import java.util.Map;

public class Desensitizer {
    private final Map<String, IDesensitizationAlgorithm> columnAlgorithmMap = new HashMap<>();

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

    /**
     * 根据列名获取对应的脱敏算法
     *
     * @param columnName 列名
     * @return 对应的脱敏算法
     */
    public IDesensitizationAlgorithm getAlgorithm(String columnName) {
        return columnAlgorithmMap.get(columnName);
    }

    /**
     * 根据列名和原始值进行脱敏
     *
     * @param columnName 列名
     * @param source     原始数据
     * @return 脱敏后的数据
     */
    public String desensitize(String columnName, String source) {
        IDesensitizationAlgorithm algorithm = getAlgorithm(columnName);
        if (algorithm != null) {
            return algorithm.desensitize(source);
        }
        return source; // 没有注册脱敏算法则返回原始数据
    }

}
