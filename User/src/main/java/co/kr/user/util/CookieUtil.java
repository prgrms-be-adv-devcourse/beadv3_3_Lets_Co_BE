package co.kr.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Optional;

@Component
public class CookieUtil {

    // 유지보수를 위해 쿠키 이름과 만료 시간을 상수로 관리
    public static final String REFRESH_TOKEN_NAME = "refreshToken";
    public static final String ACCESS_TOKEN_NAME = "accessT oken";
    public static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60; // 7일

    /**
     * 쿠키 생성 및 추가
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);  // XSS 방지
        cookie.setSecure(true);    // HTTPS 환경에서만 전송 (운영 시 필수)
        cookie.setMaxAge(maxAge);

        // SameSite 설정을 위해선 ResponseHeader에 직접 넣거나 Spring ResponseCookie 사용 권장
        // 일단 기본 Servlet Cookie 방식 유지
        response.addCookie(cookie);
    }

    /**
     * 쿠키 조회
     */
    public static Optional<String> getCookieValue(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return Optional.empty();

        return Arrays.stream(request.getCookies())
                .filter(cookie -> cookie.getName().equals(name))
                .map(Cookie::getValue)
                .findFirst();
    }

    /**
     * 쿠키 삭제 (MaxAge 0으로 설정하여 덮어쓰기)
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setMaxAge(0); // 즉시 만료
        response.addCookie(cookie);
    }
}