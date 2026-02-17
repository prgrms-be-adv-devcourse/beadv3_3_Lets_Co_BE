package co.kr.user.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * CookieUtil 단위 테스트
 * 수정 사항: ArgumentCaptor.forClass 내 오타 교정 (Cookie.class)
 */
@DisplayName("CookieUtil 단위 테스트")
class CookieUtilTest {

    @Test
    @DisplayName("쿠키 추가 테스트: 설정된 이름, 값, 만료시간이 응답 헤더에 정확히 담겨야 함")
    void addCookieTest() {
        // Given
        HttpServletResponse response = mock(HttpServletResponse.class);
        String name = "testCookie";
        String value = "testValue";
        int maxAge = 3600;

        // 쿠키 객체를 캡처하기 위한 설정 (정상 수정됨)
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        // When
        CookieUtil.addCookie(response, name, value, maxAge);

        // Then
        // response.addCookie()가 호출되었는지 확인하고 전달된 쿠키 객체를 캡처
        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();

        assertEquals(name, capturedCookie.getName());
        assertEquals(value, capturedCookie.getValue());
        assertEquals("/", capturedCookie.getPath());
        assertTrue(capturedCookie.isHttpOnly()); // 보안 속성 확인
        assertEquals(maxAge, capturedCookie.getMaxAge());
    }

    @Test
    @DisplayName("쿠키 삭제 테스트: MaxAge가 0으로 설정되어 즉시 만료되어야 함")
    void deleteCookieTest() {
        // Given
        HttpServletResponse response = mock(HttpServletResponse.class);
        String name = "deleteTarget";
        ArgumentCaptor<Cookie> cookieCaptor = ArgumentCaptor.forClass(Cookie.class);

        // When
        CookieUtil.deleteCookie(response, name);

        // Then
        verify(response).addCookie(cookieCaptor.capture());
        Cookie capturedCookie = cookieCaptor.getValue();

        assertEquals(name, capturedCookie.getName());
        assertEquals(0, capturedCookie.getMaxAge()); // 0이면 브라우저가 즉시 삭제함
        assertTrue(capturedCookie.isHttpOnly());
    }
}