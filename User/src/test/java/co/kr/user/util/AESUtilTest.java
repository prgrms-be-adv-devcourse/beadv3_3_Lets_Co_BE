package co.kr.user.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AESUtil 단위 테스트")
class AESUtilTest {

    private AESUtil aesUtil;
    private final String testKey = "test-secret-key-32-bytes-length!!!"; // 32바이트 테스트 키

    @BeforeEach
    void setUp() {
        aesUtil = new AESUtil();
        // @Value 필드에 강제로 테스트 키 주입
        ReflectionTestUtils.setField(aesUtil, "secretKey", testKey);
        // @PostConstruct 메서드 수동 호출하여 초기화
        aesUtil.init();
    }

    @Test
    @DisplayName("GCM 모드 암복호화 테스트: 매번 다른 암호문이 생성되지만 복호화 결과는 같아야 함")
    void gcmTest() {
        // Given
        String plainText = "Sensitive Data 123";

        // When
        String encrypted1 = aesUtil.GCMencrypt(plainText);
        String encrypted2 = aesUtil.GCMencrypt(plainText);
        String decrypted = aesUtil.GCMdecrypt(encrypted1);

        // Then
        assertNotEquals(encrypted1, encrypted2); // GCM은 랜덤 IV를 사용하므로 암호문이 달라야 함
        assertEquals(plainText, decrypted);      // 하지만 복호화 결과는 원문과 같아야 함
    }

    @Test
    @DisplayName("CBC 모드 암복호화 테스트: 동일 평문은 동일 암호문을 생성해야 함 (결정적 암호화)")
    void cbcTest() {
        // Given
        String plainText = "Searchable Email @ test.com";

        // When
        String encrypted1 = aesUtil.CBCencrypt(plainText);
        String encrypted2 = aesUtil.CBCencrypt(plainText);
        String decrypted = aesUtil.CBCdecrypt(encrypted1);

        // Then
        assertEquals(encrypted1, encrypted2); // 검색용 CBC는 동일한 결과를 내야 함
        assertEquals(plainText, decrypted);
    }
}