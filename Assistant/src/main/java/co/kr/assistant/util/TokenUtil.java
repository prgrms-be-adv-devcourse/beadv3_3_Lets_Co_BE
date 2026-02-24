package co.kr.assistant.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Base64;
import java.util.Map;

/**
 * 토큰 추출 및 페이로드 분석을 담당하는 최소화된 유틸리티 클래스입니다.
 */
public class TokenUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private TokenUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * HttpServletRequest에서 특정 이름의 쿠키 값을 추출합니다.
     */
    public static String getCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    public static Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            Object userIdx = claims.get("sub");
            return userIdx != null ? Long.valueOf(userIdx.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String getClientIp(HttpServletRequest request) {
        // 확인해야 할 주요 HTTP 헤더들
        String[] headerNames = {
                "X-Forwarded-For",    // 가장 일반적인 프록시 헤더
                "Proxy-Client-IP",    // Apache HTTP Server에서 사용하는 경우
                "WL-Proxy-Client-IP", // WebLogic에서 사용하는 경우
                "HTTP_CLIENT_IP",     // 일부 PHP 환경이나 오래된 프록시
                "HTTP_X_FORWARDED_FOR"
        };

        for (String header : headerNames) {
            String ip = request.getHeader(header);

            // 헤더가 존재하고 값이 "unknown"이 아닌 경우 탐색
            if (ip != null && !ip.isEmpty() && !"unknown".equalsIgnoreCase(ip)) {
                // X-Forwarded-For의 경우 '클라이언트IP, 프록시1IP, 프록시2IP' 형태로 올 수 있음
                // 따라서 첫 번째 IP가 실제 클라이언트의 외부 IP임
                if (ip.contains(",")) {
                    return ip.split(",")[0].trim();
                }
                return ip;
            }
        }

        // 헤더에 정보가 없으면 최후의 수단으로 직접 연결된 주소를 가져옴
        // (이 경우 로컬 테스트 시에는 127.0.0.1이 반환됨)
        return request.getRemoteAddr();
    }
}