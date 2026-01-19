package co.kr.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

/**
 * HTTP 쿠키(Cookie) 생성, 조회, 삭제를 돕는 유틸리티 클래스입니다.
 * JWT 토큰(Access/Refresh Token)을 클라이언트 브라우저에 안전하게 저장하기 위해 주로 사용됩니다.
 * 정적(static) 메서드로 구성되어 있어 인스턴스 생성 없이 바로 사용할 수 있습니다.
 */
public class CookieUtil {
    // 엑세스 토큰용 쿠키 이름 상수
    public static final String ACCESS_TOKEN_NAME = "accessToken";

    // 리프레시 토큰용 쿠키 이름 상수
    public static final String REFRESH_TOKEN_NAME = "refreshToken";

    // 엑세스 토큰 유효 시간 (초 단위) - 15분 (15 * 60)
    public static final int ACCESS_TOKEN_EXPIRY = 15 * 60;

    // 리프레시 토큰 유효 시간 (초 단위) - 7일 (7 * 24 * 60 * 60)
    public static final int REFRESH_TOKEN_EXPIRY = 7 * 24 * 60 * 60;

    /**
     * 새로운 쿠키를 생성하여 HTTP 응답(Response)에 추가하는 메서드입니다.
     * 보안을 강화하기 위해 HttpOnly 속성을 true로 설정하고, 모든 경로("/")에서 유효하도록 설정합니다.
     *
     * @param response HTTP 응답 객체 (쿠키를 담을 컨테이너)
     * @param name 쿠키의 이름 (Key)
     * @param value 쿠키의 값 (Value) - 주로 토큰 문자열
     * @param maxAge 쿠키의 만료 시간 (초 단위)
     */
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/"); // 애플리케이션의 모든 경로에서 쿠키 접근 허용
        cookie.setHttpOnly(true); // 자바스크립트를 통한 쿠키 접근 차단 (XSS 방지)
        cookie.setMaxAge(maxAge); // 쿠키 생존 시간 설정

        // HTTPS 환경에서만 쿠키를 전송하려면 아래 주석 해제 (Secure 옵션)
        // cookie.setSecure(true);

        response.addCookie(cookie); // 응답 헤더에 Set-Cookie 추가
    }

    /**
     * 특정 이름의 쿠키를 삭제하는 메서드입니다.
     * 브라우저에서 쿠키를 바로 삭제하는 API는 없으므로, 만료 시간을 0으로 설정한 동일한 이름의 쿠키를 덮어씌워 삭제 효과를 냅니다.
     *
     * @param response HTTP 응답 객체
     * @param name 삭제할 쿠키의 이름
     */
    public static void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, null); // 값은 의미 없으므로 null 또는 빈 문자열
        cookie.setPath("/"); // 생성 시와 동일한 경로 설정 필수
        cookie.setHttpOnly(true); // 생성 시와 동일한 옵션 설정 권장
        cookie.setMaxAge(0); // 만료 시간을 0으로 설정하여 즉시 만료(삭제) 처리

        response.addCookie(cookie);
    }
}