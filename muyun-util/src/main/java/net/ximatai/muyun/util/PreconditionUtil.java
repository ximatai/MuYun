package net.ximatai.muyun.util;

public class PreconditionUtil {
    private PreconditionUtil() {
    }

    public static void require(boolean value) {
        require(value, () -> "条件不满足");
    }

    public static void require(boolean value, LazyMessageFunction lazyMessage) {
        if (!value) {
            final var message = lazyMessage.getMessage();
            requireNotNull(message, () -> "消息不能为空");
            throw new IllegalArgumentException(message.toString());
        }
    }

    public static <T> T requireNotNull(T value) {
        return requireNotNull(value, () -> "值不能为空");
    }

    public static <T> T requireNotNull(T value, LazyMessageFunction lazyMessage) {
        if (value == null) {
            var message = lazyMessage.getMessage();
            throw new IllegalArgumentException(message.toString());
        } else {
            return value;
        }
    }
}
