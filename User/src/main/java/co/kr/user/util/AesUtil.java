package co.kr.user.util;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * AES-256 알고리즘을 이용한 양방향 암호화/복호화 유틸리티 클래스입니다.
 * 데이터베이스에 저장되는 민감한 개인정보(이름, 전화번호, 주소, 카드정보 등)를 보호하기 위해 사용됩니다.
 * application.properties(또는 yml)에 설정된 비밀키(Secret Key)를 기반으로 동작합니다.
 */
@Component // 스프링 빈으로 등록하여 다른 컴포넌트에서 주입받아 사용할 수 있게 합니다.
public class AESUtil {

    // application.yml 파일에서 'aes.secret-key' 프로퍼티 값을 읽어옵니다.
    @Value("${custom.security.ase256.key}")
    private String secretKey;

    // 암호화 알고리즘 지정 (AES)
    private static final String ALGORITHM = "AES";

    // 암호화 키 객체
    private SecretKeySpec secretKeySpec;

    /**
     * 빈 초기화 후 실행되는 메서드입니다.
     * 프로퍼티에서 읽어온 문자열 키를 기반으로 SecretKeySpec 객체를 생성합니다.
     */
    @PostConstruct
    public void init() {
        // 키 문자열을 바이트 배열로 변환하여 AES 키 스펙 생성
        this.secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
    }

    /**
     * 평문(Plain Text)을 AES 알고리즘으로 암호화하는 메서드입니다.
     *
     * @param value 암호화할 원본 문자열
     * @return Base64로 인코딩된 암호문 (String)
     * @throws RuntimeException 암호화 과정 중 오류 발생 시
     */
    public String encrypt(String value) {
        try {
            // Cipher 객체를 AES 암호화 모드로 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            // 문자열을 바이트로 변환하여 암호화 수행
            byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));

            // 암호화된 바이트 배열을 Base64 문자열로 변환하여 반환 (DB 저장 용이성)
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            // 예외 발생 시 런타임 예외로 포장하여 던짐 (로그 등을 남길 수 있음)
            throw new RuntimeException("Error while encrypting: " + e.toString(), e);
        }
    }

    /**
     * 암호문(Encrypted Text)을 복호화하여 평문으로 되돌리는 메서드입니다.
     *
     * @param encryptedValue Base64로 인코딩된 암호문
     * @return 복호화된 원본 문자열
     * @throws RuntimeException 복호화 과정 중 오류 발생 시
     */
    public String decrypt(String encryptedValue) {
        try {
            // Cipher 객체를 AES 복호화 모드로 초기화
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            // Base64 문자열을 바이트 배열로 디코딩
            byte[] original = Base64.getDecoder().decode(encryptedValue);

            // 복호화 수행 후 바이트 배열을 다시 문자열로 변환하여 반환
            byte[] decrypted = cipher.doFinal(original);
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.toString(), e);
        }
    }
}