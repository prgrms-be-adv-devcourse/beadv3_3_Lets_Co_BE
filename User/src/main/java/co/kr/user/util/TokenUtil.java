package co.kr.user.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Base64;
import java.util.Map;

/**
 * 토큰 만료 임박 여부를 확인하는 보조 유틸리티 클래스입니다.
 * 서명 검증 없이 Payload만 디코딩하여 빠르게 만료 시간을 체크할 때 사용합니다.
 */
public class TokenUtil {

    // 만료 임박 기준 시간 (6일 = 518,400초)
    private static final long THREE_DAYS_IN_SECONDS = 518400;

    /**
     * 토큰의 만료 시간이 6일 미만으로 남았는지 확인합니다.
     * Refresh Token Rotation(RTR) 로직에서 토큰 교체 여부를 판단할 때 사용됩니다.
     *
     * @param token 검사할 JWT 토큰
     * @return 만료까지 6일 미만이면 true, 아니면 false
     */
    public static boolean isTokenExpiringSoon(String token) {
        try {
            // JWT는 Header.Payload.Signature 구조이므로 Payload(인덱스 1) 추출
            String[] parts = token.split("\\.");
            if (parts.length < 2) return false;

            // Payload를 Base64 URL 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // JSON 문자열을 Map으로 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // exp(만료시간) 클레임 확인
            Integer exp = (Integer) claims.get("exp");
            if (exp == null) return false;

            long now = System.currentTimeMillis() / 1000;

            // 남은 시간이 기준 시간(6일)보다 적은지 확인
            return (exp - now) < THREE_DAYS_IN_SECONDS;

        } catch (Exception e) {
            // 파싱 실패 등의 경우 갱신하지 않음으로 처리
            return false;
        }
    }
}