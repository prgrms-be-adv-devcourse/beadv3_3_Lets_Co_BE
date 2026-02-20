package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Base64Util 단위 테스트")
class Base64UtilTest {

    @Test
    @DisplayName("Base64 인코딩 및 디코딩 통합 테스트: 원본 데이터가 복구되어야 함")
    void encodeAndDecodeTest() {
        // Given
        String originalData = "Hello World! 안녕하세요!";

        // When
        String encodedData = Base64Util.encode(originalData);
        String decodedData = Base64Util.decode(encodedData);

        // Then
        assertNotEquals(originalData, encodedData); // 인코딩된 데이터는 원본과 달라야 함
        assertEquals(originalData, decodedData);    // 디코딩 후 원본 데이터와 일치해야 함
    }

    @Test
    @DisplayName("빈 문자열 및 특수문자 처리 테스트")
    void specialCharacterTest() {
        // Given
        String specialStr = "{}[]()!@#$%^&*";

        // When
        String encoded = Base64Util.encode(specialStr);
        String decoded = Base64Util.decode(encoded);

        // Then
        assertEquals(specialStr, decoded);
    }
}