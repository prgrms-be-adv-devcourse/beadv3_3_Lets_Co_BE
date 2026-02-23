package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWTUtil 단위 테스트")
class JWTUtilTest {

    // 테스트용 설정값 (Base64 인코딩된 키 필요)
    private final String accessSecret = "Y29tLnRlc3Quc2VjcmV0LWFjY2Vzcy10b2tlbi1rZXktYmFzZTY0";
    private final String refreshSecret = "Y29tLnRlc3Quc2VjcmV0LXJlZnJlc2gtdG9rZW4ta2V5LWJhc2U2NA==";
    private final long accessExp = 3600000;  // 1시간
    private final long refreshExp = 86400000; // 1일

    private final JWTUtil jwtUtil = new JWTUtil(accessSecret, accessExp, refreshSecret, refreshExp);

    @Test
    @DisplayName("액세스 토큰 생성 및 정보 추출 테스트")
    void accessTokenTest() {
        // Given
        Long userIdx = 100L;
        LocalDateTime now = LocalDateTime.now();

        // When
        String token = jwtUtil.createAccessToken(userIdx, now, now);
        Long extractedIdx = jwtUtil.getUserIdxFromToken(token, true);

        // Then
        assertNotNull(token);
        assertEquals(userIdx, extractedIdx); // 토큰에서 꺼낸 ID가 일치해야 함
    }

    @Test
    @DisplayName("리프레시 토큰 검증 테스트")
    void refreshTokenValidationTest() {
        // Given
        Long userIdx = 200L;
        String token = jwtUtil.createRefreshToken(userIdx);

        // When
        boolean isValid = jwtUtil.validateRefreshToken(token);
        boolean isInvalid = jwtUtil.validateRefreshToken("invalid.token.here");

        // Then
        assertTrue(isValid);   // 정상 토큰은 true
        assertFalse(isInvalid); // 잘못된 토큰은 false
    }
}