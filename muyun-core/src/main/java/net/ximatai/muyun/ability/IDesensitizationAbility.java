package net.ximatai.muyun.ability;

import net.ximatai.muyun.core.desensitization.Desensitizer;

import java.util.Map;

/**
 * 数据脱敏的能力
 */
public interface IDesensitizationAbility {

    Desensitizer getDesensitizer();

    default void desensitize(Map<String, Object> map) {
        Desensitizer desensitizer = getDesensitizer();
        if (desensitizer == null) return;

        map.replaceAll((k, v) -> v != null ? desensitizer.desensitize(k, v.toString()) : null);
    }
}
