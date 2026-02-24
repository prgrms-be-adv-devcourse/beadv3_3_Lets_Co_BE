package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * EmailTemplateProvider(java/co/kr/user/util/EmailTemplateProvider.java 참고) 단위 테스트
 */
@DisplayName("EmailTemplateProvider 단위 테스트")
class EmailTemplateProviderTest {

    private final EmailTemplateProvider templateProvider = new EmailTemplateProvider();

    @Test
    @DisplayName("회원가입 템플릿 생성 테스트: 인증 코드가 HTML 본문에 포함되어야 함")
    void signupTemplateTest() {
        // Given
        String code = "SIGNUP123";

        // When
        String html = templateProvider.getSignupTemplate(code);

        // Then
        assertNotNull(html);
        assertTrue(html.contains(code), "템플릿에 인증 코드가 포함되어 있어야 합니다.");
        assertTrue(html.contains("이메일 인증 안내"), "템플릿 제목이 올바르지 않습니다.");
    }

    @Test
    @DisplayName("판매자 승인 안내 템플릿 테스트: 상점명이 정확히 표시되어야 함")
    void sellerApprovalTemplateTest() {
        // Given
        String sellerName = "우리가게";

        // When
        String html = templateProvider.getSellerApprovalTemplate(sellerName);

        // Then
        assertTrue(html.contains(sellerName));
        assertTrue(html.contains("판매자 등록 완료 안내"));
    }
}