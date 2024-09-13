package net.ximatai.muyun.util;

public class StringUtil {

    public static boolean isBlank(Object x) {
        return switch (x) {
            case null -> true;
            case String str -> str.isBlank() || "NULL".equalsIgnoreCase(str.trim());
            default -> false;
        };
    }

    public static boolean isNotBlank(Object x) {
        return !isBlank(x);
    }
}
