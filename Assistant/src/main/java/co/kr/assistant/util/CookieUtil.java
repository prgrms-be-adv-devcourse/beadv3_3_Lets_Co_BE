package co.kr.assistant.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 쿠키 생성, 설정, 삭제 등을 처리하는 유틸리티 클래스입니다.
 * JWT 토큰 및 챗봇 세션(chatToken)을 쿠키에 저장하고 관리하는 데 사용됩니다.
 * XSS 및 CSRF 공격 방어를 위한 보안 속성이 강화되었습니다.
 */
public class CookieUtil {

    public static final String CHAT_TOKEN_NAME = "chatToken";
    public static final int CHAT_TOKEN_EXPIRY = 60 * 60;

    /**
     * 유틸리티 클래스이므로 인스턴스 생성을 방지하기 위해 생성자를 private으로 선언합니다.
     */
    private CookieUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 쿠키를 생성하여 응답(Response)에 추가합니다.
     * 보안을 위해 HttpOnly, Secure, SameSite=Strict 속성을 엄격하게 설정합니다.
     * * @param response HttpServletResponse 객체
     * @param name 쿠키 이름
     * @param value 쿠키 값 (토큰 문자열 등)
     * @param maxAge 쿠키 만료 시간 (초 단위)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 모든 경로에서 쿠키 접근 가능

        // 1. XSS 공격 방어: JavaScript(document.cookie)를 통한 토큰 탈취 원천 차단
        cookie.setHttpOnly(true);

        // 2. 네트워크 스니핑 방어: 오직 HTTPS 암호화 통신에서만 쿠키 전송 허용
        // (주의: 로컬 환경(http://localhost)에서 테스트 시 브라우저가 쿠키를 저장하지 않을 수 있습니다.
        // 로컬 테스트가 필요하다면 개발 환경 설정에 따라 잠시 주석 처리할 수 있습니다.)
        cookie.setSecure(true);

        // 3. CSRF 공격 방어: 외부 사이트(Cross-Site)에서 출발하는 요청에는 쿠키 전송을 완전 차단
        cookie.setAttribute("SameSite", "Strict");

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

        // 삭제할 때도 생성 시와 동일한 보안 속성을 부여하여 확실히 덮어쓰도록 유도
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setAttribute("SameSite", "Strict");

        cookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 즉시 삭제
        response.addCookie(cookie);
    }
}