package net.ximatai.muyun.database.tool;

import jakarta.persistence.Tuple;

import java.util.HashMap;
import java.util.Map;

public class TupleTool {
    public static Map<String, Object> toMap(Tuple tuple) {
        Map<String, Object> result = new HashMap<>();
        if (tuple == null) {
            return result;
        }
        tuple.getElements().forEach(tupleElement -> {
            String alias = tupleElement.getAlias();
            Object value = tuple.get(alias);
            result.put(alias, value);
        });

        return result;
    }
}
