package co.kr.user.util;

import co.kr.user.model.dto.mail.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 이메일 발송 기능을 담당하는 유틸리티 클래스입니다.
 * Spring의 JavaMailSender를 사용하여 실제 메일을 전송합니다.
 * 비동기(Async) 처리를 통해 이메일 발송 중 메인 스레드가 차단(Block)되지 않도록 합니다.
 */
@Slf4j // 로깅 기능을 위한 Lombok 어노테이션 (log 객체 자동 생성)
@Component
@RequiredArgsConstructor
public class MailUtil {
    // Spring Boot의 MailSender 인터페이스 구현체 주입
    private final JavaMailSender javaMailSender;

    /**
     * 이메일을 비동기로 전송합니다.
     * @param emailMessage 수신자, 제목, 본문 내용이 담긴 DTO 객체
     * @param isHtml 본문이 HTML 형식인지 여부 (true: HTML, false: 일반 텍스트)
     */
    @Async // 이 메서드는 별도의 스레드에서 비동기로 실행됩니다. (응답 대기 시간 최소화)
    public void sendEmail(EmailMessage emailMessage, boolean isHtml) {
        // MIME 형식의 메일 메시지 객체 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            // MimeMessageHelper를 사용하여 멀티파트(Multipart) 메시지를 쉽게 구성
            // 두 번째 인자 false: 멀티파트 모드 아님 (첨부파일 없음), 세 번째 인자: 인코딩 설정
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 수신자 설정
            mimeMessageHelper.setTo(emailMessage.getTo());
            // 메일 제목 설정
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            // 메일 본문 설정 (두 번째 인자가 true이면 HTML 태그가 렌더링됨)
            mimeMessageHelper.setText(emailMessage.getMessage(), isHtml);

            // 메일 전송 수행
            javaMailSender.send(mimeMessage);

            // 전송 성공 로그 기록
            log.info("Email sent successfully to: {}", emailMessage.getTo());

        } catch (MessagingException e) {
            // 전송 실패 시 에러 로그 기록 (예외를 다시 던지지 않고 로그만 남김)
            log.error("Failed to send email to: {}", emailMessage.getTo(), e);
        }
    }
}