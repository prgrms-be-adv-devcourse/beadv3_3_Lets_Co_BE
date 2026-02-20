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

    /**
     * JWT 토큰의 Payload에서 유저 식별자(userIdx)를 추출합니다.
     */
    public static Long getUserIdFromToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length < 2) return null;

            // Payload(두 번째 부분) 디코딩
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]));
            Map<String, Object> claims = objectMapper.readValue(payload, Map.class);

            Object userIdx = claims.get("userIdx");
            return userIdx != null ? Long.valueOf(userIdx.toString()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}