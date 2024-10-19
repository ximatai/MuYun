package net.ximatai.muyun.core.desensitization;

public class MaskEmailAlgorithm implements IDesensitizationAlgorithm {
    @Override
    public String desensitize(String source) {
        if (source == null || !source.contains("@")) {
            return source; // 非法邮箱不进行脱敏
        }
        String[] parts = source.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];

        if (localPart.length() <= 1) {
            return source; // 短邮箱不进行脱敏
        }

        // 只显示首尾字符，中间使用 * 号代替
        return localPart.charAt(0) + "*".repeat(localPart.length() - 2) + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
    }
}
