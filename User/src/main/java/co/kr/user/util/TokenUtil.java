package co.kr.user.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Base64;
import java.util.Map;

/**
 * JWT 토큰의 만료 임박 여부 등을 확인하는 보조 유틸리티 클래스입니다.
 * JWTUtil과 달리 서명 검증 없이 페이로드(Payload)만 빠르게 디코딩하여 정보를 확인합니다.
 */
public class TokenUtil {
    // static final로 선언하여 재사용 (메모리 절약 및 성능 향상)
    private static final ObjectMapper objectMapper = new ObjectMapper();
    // 만료 임박 기준 시간 (3일 = 3 * 24 * 60 * 60 초)
    // 리프레시 토큰 등의 갱신 시점을 판단하는 데 사용됩니다.
    private static final long THREE_DAYS_IN_SECONDS = 518400;

    /**
     * 유틸리티 클래스이므로 인스턴스 생성을 방지합니다.
     */
    private TokenUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 토큰의 만료 시간이 3일 미만으로 남았는지 확인합니다.
     * 서명 검증을 하지 않고 Base64 디코딩만 수행하므로 빠르지만,
     * 위조된 토큰일 가능성이 있으므로 신뢰할 수 있는 환경에서만 사용해야 합니다.
     * * @param token JWT 토큰 문자열 (Header.Payload.Signature)
     * @return 만료가 임박했으면(3일 미만) true, 아니면 false
     */
    public static boolean isTokenExpiringSoon(String token) {
        try {
            // JWT는 점(.)으로 구분된 3부분으로 구성됨
            String[] parts = token.split("\\.");
            // 형식이 올바르지 않으면 false 반환
            if (parts.length < 2) return false;

            // 두 번째 부분(Payload)을 Base64 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));

            // JSON 문자열을 Map으로 변환
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            // 만료 시간(exp) 클레임 추출 (Unix Timestamp, 초 단위)
            Integer exp = (Integer) claims.get("exp");
            // 만료 시간이 없으면 false 반환
            if (exp == null) return false;

            // 현재 시간 (초 단위)
            long now = System.currentTimeMillis() / 1000;

            // 남은 시간이 기준 시간(3일)보다 적은지 확인
            return (exp - now) < THREE_DAYS_IN_SECONDS;
        } catch (Exception e) {
            // 파싱 중 오류 발생 시 false 반환 (안전한 실패)
            return false;
        }
    }
}