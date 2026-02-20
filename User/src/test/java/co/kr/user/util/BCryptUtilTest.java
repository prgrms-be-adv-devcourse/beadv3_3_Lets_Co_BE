package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BCryptUtil 단위 테스트")
class BCryptUtilTest {

    private final BCryptUtil bCryptUtil = new BCryptUtil();

    @Test
    @DisplayName("비밀번호 암호화 테스트: 동일한 평문이라도 매번 다른 해시값이 생성되어야 함")
    void encodeTest() {
        // Given
        String rawPassword = "Password123!";

        // When
        String encoded1 = bCryptUtil.encode(rawPassword);
        String encoded2 = bCryptUtil.encode(rawPassword);

        // Then
        assertNotNull(encoded1);
        assertNotNull(encoded2);
        assertNotEquals(encoded1, encoded2); // BCrypt는 Salt를 사용하므로 결과가 달라야 함
        assertTrue(encoded1.startsWith("$2a$")); // BCrypt 알고리즘 식별자 확인
    }

    @Test
    @DisplayName("비밀번호 일치 검증 테스트: 올바른 비밀번호는 성공, 틀린 비밀번호는 실패해야 함")
    void checkTest() {
        // Given
        String rawPassword = "MySecurePassword";
        String wrongPassword = "WrongPassword";
        String encodedPassword = bCryptUtil.encode(rawPassword);

        // When & Then
        // 1. 일치하는 경우
        assertTrue(bCryptUtil.check(rawPassword, encodedPassword), "올바른 비밀번호는 true를 반환해야 합니다.");

        // 2. 일치하지 않는 경우
        assertFalse(bCryptUtil.check(wrongPassword, encodedPassword), "틀린 비밀번호는 false를 반환해야 합니다.");
    }
}