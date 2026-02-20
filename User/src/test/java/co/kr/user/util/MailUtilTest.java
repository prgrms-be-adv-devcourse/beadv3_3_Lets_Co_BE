package co.kr.user.util;

import co.kr.user.model.dto.mail.EmailMessage;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * MailUtil(java/co/kr/user/util/MailUtil.java) 단위 테스트
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MailUtil 단위 테스트")
class MailUtilTest {

    @Mock
    private JavaMailSender javaMailSender;

    @InjectMocks
    private MailUtil mailUtil;

    @Test
    @DisplayName("이메일 발송 테스트: JavaMailSender의 send 메서드가 호출되어야 함")
    void sendEmailTest() {
        // Given
        EmailMessage message = EmailMessage.builder()
                .to("test@example.com")
                .subject("테스트 제목")
                .message("테스트 내용")
                .build();

        // MimeMessage 생성 시 가짜 객체 반환 설정
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // When
        mailUtil.sendEmail(message, true);

        // Then
        // 실제로 메일 발송 메서드가 1번 호출되었는지 검증
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }
}