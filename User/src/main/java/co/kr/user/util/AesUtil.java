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

/**
 * [AES 암호화 유틸리티]
 * 민감한 개인정보(이름, 전화번호, 생년월일 등)를 DB에 저장할 때 암호화하고,
 * 필요할 때 다시 원래대로 복구(복호화)하기 위한 도구입니다.
 * * 알고리즘: AES-256 (키 길이가 32바이트인 경우)
 * 모드: CBC (Cipher Block Chaining) - 보안성이 높음
 * 패딩: PKCS5Padding - 데이터 길이가 블록 크기에 맞지 않을 때 채워주는 방식
 */
@Component // 스프링 빈으로 등록하여 다른 서비스에서 주입받아 사용 가능하게 함
public class AesUtil {

    // application.yml 파일에 설정된 비밀키 문자열을 가져옴
    @Value("${custom.security.ase256.key}")
    private String secretKey;

    private SecretKeySpec secretKeySpec; // 실제 암호화에 사용될 키 객체
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding"; // 알고리즘/모드/패딩 설정

    /**
     * [초기화 메서드]
     * 객체가 생성되고 의존성 주입이 완료된 후(@PostConstruct) 자동으로 실행됩니다.
     * 사용자가 입력한 문자열 키를 AES 알고리즘에 맞는 32바이트(256비트) 정규 키로 변환합니다.
     */
    @PostConstruct
    public void init() throws Exception {
        // 입력받은 키(secretKey)를 그대로 쓰지 않고 SHA-256으로 해싱합니다.
        // 이유 1: 사용자가 키 길이를 32자로 정확히 맞추지 않아도, 해시 결과는 항상 32바이트가 됩니다.
        // 이유 2: 원본 문자열의 패턴을 감추어 보안성을 높입니다.
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = sha.digest(secretKey.getBytes(StandardCharsets.UTF_8));

        // 해싱된 바이트 배열로 비밀키 객체 생성
        this.secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * [암호화 메서드]
     * 평문(String) -> 암호문(Base64 String)
     * 같은 평문이라도 실행할 때마다 결과가 달라집니다. (랜덤 IV 사용)
     */
    public String encrypt(String plainText) {
        try {
            // 1. [랜덤 IV 생성]
            // CBC 모드에서는 초기화 벡터(IV)가 필요합니다.
            // 매번 새로운 난수(16바이트)를 생성하여 암호화의 무작위성을 보장합니다.
            byte[] iv = new byte[16];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 2. [암호화 실행]
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivSpec); // 암호화 모드로 초기화
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // 3. [IV + 암호문 결합]
            // 복호화할 때 암호화에 사용된 IV가 반드시 필요합니다.
            // 따라서 결과물의 앞부분에 IV를 붙여서 함께 저장합니다.
            // 구조: [IV(16bytes)] + [암호문(나머지)]
            byte[] combined = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, iv.length); // 앞부분에 IV 복사
            System.arraycopy(encrypted, 0, combined, iv.length, encrypted.length); // 뒷부분에 암호문 복사

            // 4. [Base64 인코딩]
            // 바이너리 데이터(byte[])를 텍스트(String)로 변환하여 DB 저장을 용이하게 합니다.
            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("암호화 중 오류 발생", e);
        }
    }

    /**
     * [복호화 메서드]
     * 암호문(Base64 String) -> 평문(String)
     */
    public String decrypt(String cipherText) {
        try {
            // 1. [Base64 디코딩]
            // 문자열을 다시 바이트 배열로 변환
            byte[] decoded = Base64.getDecoder().decode(cipherText);

            // 2. [IV 추출]
            // 암호화할 때 앞 16바이트에 IV를 붙였으므로, 다시 분리해냅니다.
            byte[] iv = new byte[16];
            System.arraycopy(decoded, 0, iv, 0, 16);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            // 3. [순수 암호문 추출]
            // 전체 길이에서 IV 길이(16)를 뺀 나머지가 실제 데이터입니다.
            byte[] encrypted = new byte[decoded.length - 16];
            System.arraycopy(decoded, 16, encrypted, 0, encrypted.length);

            // 4. [복호화 실행]
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivSpec); // 복호화 모드로 초기화 (키와 추출한 IV 사용)

            // 복구된 바이트 배열을 문자열로 변환하여 반환
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("복호화 중 오류 발생", e);
        }
    }
}