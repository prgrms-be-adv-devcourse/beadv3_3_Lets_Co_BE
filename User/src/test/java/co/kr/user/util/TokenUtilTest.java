package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("TokenUtil 단위 테스트")
class TokenUtilTest {

    // 테스트를 위해 JWTUtil을 사용하여 토큰을 생성합니다.
    private final JWTUtil jwtUtil = new JWTUtil(
            "Y29tLnRlc3Quc2VjcmV0LWFjY2Vzcy10b2tlbi1rZXktYmFzZTY0", 3600000,
            "Y29tLnRlc3Quc2VjcmV0LXJlZnJlc2gtdG9rZW4ta2V5LWJhc2U2NA==", 86400000
    );

    @Test
    @DisplayName("토큰 만료 임박 확인 테스트: 만료 시간이 3일 이내면 true를 반환해야 함")
    void expiringSoonTest() {
        // Given
        // 1시간 후에 만료되는 토큰 생성 (당연히 3일 이내임)
        String shortToken = jwtUtil.createRefreshToken(1L);

        // When
        boolean isSoon = TokenUtil.isTokenExpiringSoon(shortToken);

        // Then
        assertTrue(isSoon, "1시간 후 만료되는 토큰은 '곧 만료됨' 상태여야 합니다.");
    }

    @Test
    @DisplayName("잘못된 형식의 토큰 처리 테스트")
    void invalidTokenTest() {
        // When & Then
        assertFalse(TokenUtil.isTokenExpiringSoon("plain-text-not-a-token")); // 형식 에러 시 false
    }
}