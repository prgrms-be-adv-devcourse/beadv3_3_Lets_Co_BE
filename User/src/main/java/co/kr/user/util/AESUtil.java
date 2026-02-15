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

/**
 * AES 암호화 및 복호화를 처리하는 유틸리티 클래스입니다.
 * GCM(Galois/Counter Mode) 및 CBC(Cipher Block Chaining) 모드를 지원합니다.
 */
@Component
public class AESUtil {
    // application.yml 또는 properties 파일에서 설정된 비밀키를 주입받습니다.
    @Value("${custom.security.ase256.key}")
    private String secretKey;

    // AES GCM 암호화 알고리즘 상수 (보안성이 높음, 인증 암호화 지원)
    private static final String GCM_ALGORITHM = "AES/GCM/NoPadding";
    // AES CBC 암호화 알고리즘 상수 (일반적인 블록 암호화 모드)
    private static final String CBC_ALGORITHM = "AES/CBC/PKCS5Padding";
    // GCM 태그 비트 길이 (128비트)
    private static final int TAG_BIT_LENGTH = 128;
    // GCM 초기화 벡터(IV) 크기 (12바이트 권장)
    private static final int GCM_IV_SIZE = 12;
    // CBC 초기화 벡터(IV) 크기 (16바이트 - AES 블록 크기와 동일)
    private static final int CBC_IV_SIZE = 16;
    // CBC 모드에서 사용할 고정된 IV (0으로 채워짐, 실제 운영 시 랜덤 IV 권장되나 여기서는 고정 사용)
    private static final byte[] FIXED_IV = new byte[CBC_IV_SIZE];

    // AES 비밀키 객체
    private SecretKeySpec secretKeySpec;
    // 암호학적으로 안전한 난수 생성기
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 빈 초기화 시 실행되는 메서드입니다.
     * 주입받은 문자열 비밀키를 바이트 배열로 변환하여 SecretKeySpec 객체를 생성합니다.
     */
    @PostConstruct
    public void init() {
        byte[] keyBytes = new byte[32]; // AES-256을 위한 32바이트 키 배열
        byte[] rawKey = secretKey.getBytes(StandardCharsets.UTF_8);
        // 주입받은 secretKey가 32바이트보다 짧으면 0으로 패딩되고, 길면 잘립니다.
// 운영 환경에서는 반드시 32바이트 이상의 복잡한 키를 application.yml에 설정해야 보안이 유지됩니다.
        System.arraycopy(rawKey, 0, keyBytes, 0, Math.min(rawKey.length, keyBytes.length));
        this.secretKeySpec = new SecretKeySpec(keyBytes, "AES");
    }

    /**
     * 데이터를 AES/GCM 모드로 암호화합니다.
     * @param value 암호화할 평문 문자열
     * @return Base64로 인코딩된 암호화 문자열 (IV 포함)
     */
    public String GCMencrypt(String value) {
        try {
            // 랜덤한 IV(Initial Vector) 생성
            byte[] iv = new byte[GCM_IV_SIZE];
            secureRandom.nextBytes(iv);

            // Cipher 객체 생성 및 초기화
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, spec);

            // 암호화 수행
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // 복호화를 위해 IV와 암호문을 합침 (IV + EncryptedData)
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length);
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length);

            // 결과를 Base64 문자열로 반환
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("GCM 암호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * 데이터를 AES/CBC 모드로 암호화합니다.
     * @param value 암호화할 평문 문자열
     * @return Base64로 인코딩된 암호화 문자열
     */
    public String CBCencrypt(String value) {
        try {
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            // CBC 모드는 여기서는 고정된 IV를 사용 (검색 가능성 등을 위해 결정적 암호화가 필요한 경우 사용됨)
            IvParameterSpec ivSpec = new IvParameterSpec(FIXED_IV);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec);

            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("CBC 암호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * AES/GCM 모드로 암호화된 문자열을 복호화합니다.
     * @param encryptedValue Base64로 인코딩된 암호문 (IV 포함)
     * @return 복호화된 평문 문자열
     */
    public String GCMdecrypt(String encryptedValue) {
        try {
            // Base64 디코딩
            byte[] combined = Base64.getDecoder().decode(encryptedValue);

            // 앞부분에서 IV 추출
            byte[] iv = new byte[GCM_IV_SIZE];
            System.arraycopy(combined, 0, iv, 0, iv.length);

            // 나머지 부분에서 실제 암호문 추출
            byte[] encrypted = new byte[combined.length - GCM_IV_SIZE];
            System.arraycopy(combined, GCM_IV_SIZE, encrypted, 0, encrypted.length);

            // Cipher 초기화 및 복호화
            Cipher cipher = Cipher.getInstance(GCM_ALGORITHM);
            GCMParameterSpec spec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, spec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("GCM 복호화 중 오류 발생: " + e.getMessage(), e);
        }
    }

    /**
     * AES/CBC 모드로 암호화된 문자열을 복호화합니다.
     * @param encryptedValue Base64로 인코딩된 암호문
     * @return 복호화된 평문 문자열
     */
    public String CBCdecrypt(String encryptedValue) {
        try {
            byte[] encrypted = Base64.getDecoder().decode(encryptedValue);
            Cipher cipher = Cipher.getInstance(CBC_ALGORITHM);
            // 암호화 시 사용한 것과 동일한 고정 IV 사용
            IvParameterSpec ivSpec = new IvParameterSpec(FIXED_IV);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec);

            byte[] decrypted = cipher.doFinal(encrypted);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("CBC 복호화 중 오류 발생: " + e.getMessage(), e);
        }
    }
}