package co.kr.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 쿠키 생성, 설정, 삭제 등을 처리하는 유틸리티 클래스입니다.
 * JWT 토큰(Access Token, Refresh Token)을 쿠키에 저장하고 관리하는 데 주로 사용됩니다.
 */
public class CookieUtil {
    // 액세스 토큰 쿠키 이름 상수
    public static final String ACCESS_TOKEN_NAME = "accessToken";
    // 리프레시 토큰 쿠키 이름 상수
    public static final String REFRESH_TOKEN_NAME = "refreshToken";
    // 액세스 토큰 만료 시간 (초 단위): 15분 (15 * 60)
    public static final int ACCESS_TOKEN_EXPIRY = 15 * 60;
    // 리프레시 토큰 만료 시간 (초 단위): 7일 (7 * 24 * 60 * 60)
    public static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60;

    /**
     * 유틸리티 클래스이므로 인스턴스 생성을 방지하기 위해 생성자를 private으로 선언합니다.
     */
    private CookieUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 쿠키를 생성하여 응답(Response)에 추가합니다.
     * 보안을 위해 HttpOnly 속성을 true로 설정하여 자바스크립트를 통한 접근을 차단합니다.
     * * @param response HttpServletResponse 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값 (토큰 문자열 등)
     * @param maxAge 쿠키 만료 시간 (초 단위)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능
        cookie.setHttpOnly(true); // XSS 공격 방지를 위해 JavaScript에서 쿠키 접근 불가 설정
        cookie.setMaxAge(maxAge); // 만료 시간 설정
        response.addCookie(cookie); // 응답 헤더에 쿠키 추가
    }

    /**
     * 기존 쿠키를 삭제합니다.
     * 쿠키의 MaxAge를 0으로 설정하여 클라이언트가 즉시 만료시키도록 합니다.
     * * @param response HttpServletResponse 객체
     * @param name 삭제할 쿠키 이름
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null); // 값을 null로 설정
        cookie.setPath("/"); // 생성 시 설정한 경로와 동일하게 설정해야 삭제됨
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 즉시 삭제
        response.addCookie(cookie);
    }
}