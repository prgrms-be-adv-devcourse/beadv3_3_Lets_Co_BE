package co.kr.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

public class CookieUtil {
    public static final String ACCESS_TOKEN_NAME = "accessToken";
    public static final String REFRESH_TOKEN_NAME = "refreshToken";
    public static final int ACCESS_TOKEN_EXPIRY = 15 * 60;
    public static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60;

    private CookieUtil() {
        throw new IllegalStateException("Utility class");
    }

    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(maxAge);
        response.addCookie(cookie);
    }

    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}