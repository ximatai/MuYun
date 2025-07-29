package net.ximatai.muyun.test.util;

import net.ximatai.muyun.util.PreconditionUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PreconditionUtilTest {
    @Test
    void testRequireWithoutMsg() {
        // Test with true condition
        PreconditionUtil.require(true);

        // Test with false condition, should throw IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PreconditionUtil.require(false);
        });
        assertEquals("条件不满足", exception.getMessage());
    }

    @Test
    void testRequireWithMsg() {
        // Test with true condition
        PreconditionUtil.require(true, () -> "Custom message");

        // Test with false condition, should throw IllegalArgumentException with custom message
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PreconditionUtil.require(false, () -> "Custom message");
        });
        assertEquals("Custom message", exception.getMessage());
    }

    @Test
    void testRequireNotNullWithoutMsg() {
        // Test with non-null value
        String value = "NotNull";
        String result = PreconditionUtil.requireNotNull(value);
        assertEquals(value, result);

        // Test with null value, should throw IllegalArgumentException
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PreconditionUtil.requireNotNull(null);
        });
        assertEquals("值不能为空", exception.getMessage());
    }

    @Test
    void testRequireNotNullWithMsg() {
        // Test with non-null value
        String value = "NotNull";
        String result = PreconditionUtil.requireNotNull(value, () -> "Custom message");
        assertEquals(value, result);

        // Test with null value, should throw IllegalArgumentException with custom message
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            PreconditionUtil.requireNotNull(null, () -> "Custom message");
        });
        assertEquals("Custom message", exception.getMessage());
    }
}
