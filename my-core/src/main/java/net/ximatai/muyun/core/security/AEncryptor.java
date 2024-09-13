package net.ximatai.muyun.core.security;

import net.ximatai.muyun.core.exception.InvalidSignatureException;
import net.ximatai.muyun.util.StringUtil;

public abstract class AEncryptor {

    public void checkSign(String source, String sign) {
        if (StringUtil.isBlank(sign)) return;
        if (StringUtil.isBlank(source)) return;
        if (!sign.equals(sign(source))) {
            throw new InvalidSignatureException("数据「%s」已被篡改".formatted(source));
        }
    }

    abstract public String sign(String source);

    abstract public String encrypt(String source);

    abstract public String decrypt(String encodeText);
}
