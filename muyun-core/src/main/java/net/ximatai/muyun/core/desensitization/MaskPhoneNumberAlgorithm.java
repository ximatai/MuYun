package net.ximatai.muyun.core.desensitization;

public class MaskPhoneNumberAlgorithm implements IDesensitizationAlgorithm {
    @Override
    public String desensitize(String source) {
        if (source == null || source.length() != 11) {
            return source; // 非法手机号不进行脱敏
        }
        // 只显示前3位和后4位，中间使用 * 号代替
        return source.substring(0, 3) + "****" + source.substring(7);
    }
}
