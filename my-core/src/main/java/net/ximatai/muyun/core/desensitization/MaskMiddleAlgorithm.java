package net.ximatai.muyun.core.desensitization;

public class MaskMiddleAlgorithm implements IDesensitizationAlgorithm {
    @Override
    public String desensitize(String source) {
        if (source == null || source.length() <= 2) {
            return source; // 短字符串不进行脱敏
        }
        // 只显示首尾字符，中间使用 * 号代替
        int length = source.length();
        return source.charAt(0) + "*".repeat(length - 2) + source.charAt(length - 1);
    }
}
