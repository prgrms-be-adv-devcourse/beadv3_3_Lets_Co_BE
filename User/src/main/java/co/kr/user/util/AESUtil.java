package co.kr.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.security.SecureRandom;

@Component
public class AESUtil {
    @Value("${custom.security.ase256.key}")
    private String secretKey;

    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    private static final String CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int TAG_BIT_LENGTH = 128;
    private static final int GCM_IV_SIZE = 12;
    private static final int CBC_IV_SIZE = 16;
    private static final byte[] FIXED_IV = new byte[CBC_IV_SIZE];

    private SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    @PostConstruct
    public void init() {
        byte[] keyBytes = new byte[32];
        byte[] rawKey = secretKey.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, keyBytes.length));
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    public String GCMencrypt(String value) {
        try {
            byte[] iv = new byte[GCM_IV_SIZE];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("GCM 암호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public String CBCencrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(FIXED_IV);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("CBC 암호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public String GCMdecrypt(String encryptedValue) {
        try {
            byte[] combined = Base64.getDecoder().decode(encryptedValue);

            byte[] iv = new byte[GCM_IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            byte[] encrypted = new byte[combined.length - GCM_IV_SIZE];
            System.arraycopy(combined, GCM_IV_SIZE, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("GCM 복호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    public String CBCdecrypt(String encryptedValue) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedValue);
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(FIXED_IV);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);
            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("CBC 복호화 중 오류 발생: " + e.getMessage(), e);
        }
    }
}