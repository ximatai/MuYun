package net.ximatai.muyun.core.security;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.SM3Digest;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

@ApplicationScoped
public class SMEncryptor extends AbstractEncryptor {

    private final Logger logger = LoggerFactory.getLogger(SMEncryptor.class);

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    private PublicKey publicKey;
    private PrivateKey privateKey;

    @PostConstruct
    public void init() throws Exception {
        String publicKeyBase64 = "MFkwEwYHKoZIzj0CAQYIKoEcz1UBgi0DQgAEWAhNff78v0bA5cBVsxTIufl+fCuWu4nAvpr7AJhfHN24Ad4qF7KdsRHsVjMGJvqnC3BMLEE8sCCTa95e+ujF2w==";
        publicKey = KeyFactory.getInstance("EC")
            .generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(publicKeyBase64)));

        String privateKeyBase64 = "MIGTAgEAMBMGByqGSM49AgEGCCqBHM9VAYItBHkwdwIBAQQgTuA7s/e3iFmsiaI/JibI16CSS/Jj/rV2WXhfs9H+cRKgCgYIKoEcz1UBgi2hRANCAARYCE19/vy/RsDlwFWzFMi5+X58K5a7icC+mvsAmF8c3bgB3ioXsp2xEexWMwYm+qcLcEwsQTywIJNr3l766MXb";
        privateKey = KeyFactory.getInstance("EC")
            .generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKeyBase64)));
    }

    @Override
    public String sign(String source) {
        byte[] data = source.getBytes();
        Digest digest = new SM3Digest();
        digest.update(data, 0, data.length);
        byte[] hash = new byte[digest.getDigestSize()];
        digest.doFinal(hash, 0);
        return Hex.toHexString(hash);
    }

    @Override
    public String encrypt(String source) {
        try {
            Cipher cipher = Cipher.getInstance("SM2", "BC");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            byte[] encryptedData = cipher.doFinal(source.getBytes());
            return Hex.toHexString(encryptedData);
        } catch (Exception e) {
            logger.error("encrypt error");
            throw new RuntimeException(e);
        }
    }

    @Override
    public String decrypt(String encodeText) {
        try {
            Cipher cipher = Cipher.getInstance("SM2", "BC");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] decryptedData = cipher.doFinal(Hex.decode(encodeText));
            return new String(decryptedData);
        } catch (Exception e) {
            logger.error("decrypt error");
            throw new RuntimeException(e);
        }

    }
}
