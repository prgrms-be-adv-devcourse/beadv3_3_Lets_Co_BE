package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RandomCodeUtil 단위 테스트")
class RandomCodeUtilTest {

    private final RandomCodeUtil randomCodeUtil = new RandomCodeUtil();

    @Test
    @DisplayName("코드 생성 테스트: 설정된 범위(10~29자) 내의 길이를 가져야 함")
    void getCodeLengthTest() {
        // When
        String code = randomCodeUtil.getCode();

        // Then
        assertNotNull(code);
        assertTrue(code.length() >= 10 && code.length() < 30,
                "코드 길이는 10자 이상 30자 미만이어야 합니다. 생성된 길이: " + code.length());
    }

    @Test
    @DisplayName("코드 중복 발생 여부 확인: 연속 생성 시 높은 확률로 유니크해야 함")
    void getCodeUniquenessTest() {
        // Given
        Set<String> codes = new HashSet<>();
        int attemptCount = 1000;

        // When
        for (int i = 0; i < attemptCount; i++) {
            codes.add(randomCodeUtil.getCode());
        }

        // Then
        // 1000번 생성 시 중복이 거의 없어야 함 (SecureRandom 특성상 매우 희박)
        assertEquals(attemptCount, codes.size(), "1000번의 시도 중 중복된 코드가 발생했습니다.");
    }

    @Test
    @DisplayName("코드 구성 문자 확인: 정의된 문자셋 내의 문자로만 구성되어야 함")
    void getCodeCharacterValidationTest() {
        // Given
        String allowedChars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        String code = randomCodeUtil.getCode();

        // When & Then
        for (char c : code.toCharArray()) {
            assertTrue(allowedChars.indexOf(c) >= 0,
                    "허용되지 않은 문자가 포함되어 있습니다: " + c);
        }
    }
}