package co.kr.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

@Component // 스프링 빈으로 등록하여 다른 서비스에서 주입받아 사용 가능하게 함
public class AESUtil {
    @Value("${custom.security.ase256.key}")
    private String secretKey;

    private SecretKeySpec secretKeySpec;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    @PostConstruct
    public void init() throws Exception {
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));

        this.secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    public String encrypt(String plainText) {
        try {
            byte[] iv = new byte[16];

            new SecureRandom().nextBytes(iv);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] combined = new byte[iv.length + encrypted.length];

            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("암호화 중 오류 발생", e);
        }
    }

    public String decrypt(String cipherText) {
        try {
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            byte[] iv = new byte[16];

            System.arraycopy(decoded, 0, iv, 0, 16);

            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] encrypted = new byte[decoded.length - 16];

            System.arraycopy(decoded, 16, encrypted, 0, encrypted.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("복호화 중 오류 발생", e);
        }
    }
}