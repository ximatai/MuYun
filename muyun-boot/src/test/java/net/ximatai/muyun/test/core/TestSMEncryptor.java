package net.ximatai.muyun.test.core;

import net.ximatai.muyun.core.security.SMEncryptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSMEncryptor {

    SMEncryptor smEncryptor = new SMEncryptor();

    @BeforeEach
    void beforeEach() throws Exception {
        smEncryptor.init();
    }

    @Test
    @DisplayName("测试签名功能")
    void sign() {
        assertEquals("80b737c798f5bb0d826a987b0289e110d2283bb13d124aba4ec183644a05bb65", smEncryptor.sign("hello world!"));
    }

    @Test
    @DisplayName("测试加密和解密功能")
    void encryptAndDecrypt() {
        String source = "hello world!";
        String encodeText = smEncryptor.encrypt(source);
        String decodeText = smEncryptor.decrypt(encodeText);

        assertEquals(source, decodeText);
    }

    @Test
    @DisplayName("测试解密功能")
    void testDecrypt() {
        String source = "hello world!";
        String encodeText = "04863727249ca5f6256879937445552f513999b04fe0d46eac089006c9fd958c6416b8929989a8fbd6c24c4fc0ac1458931c9c00ff8502124162445cd4985e09f54518c5d15332143cd01fde65febb84f315a1c8e2328b0d32f3c452bd7f51f7a76dae0532eeb1c31d9dead16b";
        String decodeText = smEncryptor.decrypt(encodeText);
        assertEquals(source, decodeText);
    }

}
