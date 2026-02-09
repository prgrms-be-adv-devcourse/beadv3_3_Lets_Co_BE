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
 * 이메일 발송 기능을 제공하는 유틸리티 컴포넌트입니다.
 * Spring의 JavaMailSender를 사용하여 HTML 형식의 이메일을 전송합니다.
 * 인증 번호 발송, 알림 메일 전송 등에 활용됩니다.
 */
@Slf4j // 로깅(Logging) 기능을 활성화합니다 (발송 실패 시 로그 기록 등).
@Component // 스프링 빈으로 등록합니다.
@RequiredArgsConstructor // final 필드(javaMailSender) 생성자 주입 자동화
public class MailUtil {

    private final JavaMailSender javaMailSender;

    /**
     * 이메일을 발송하는 메서드입니다.
     * EmailMessage 객체에 담긴 수신자, 제목, 내용을 바탕으로 메일을 전송합니다.
     *
     * @param emailMessage 이메일 정보 DTO (To, Subject, Message)
     * @param isHtml 본문이 HTML 형식인지 여부 (true: HTML, false: 텍스트)
     * @return 발송 성공 시 true, 실패 시 false
     */
    @Async
    public boolean sendEmail(EmailMessage emailMessage, boolean isHtml) {
        // MIME(Multipurpose Internet Mail Extensions) 타입의 메시지 객체 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            // MimeMessageHelper를 사용하여 복잡한 MIME 메시지 설정을 간편하게 처리
            // 두 번째 인자(false)는 멀티파트(첨부파일 등) 사용 여부, 세 번째 인자는 인코딩 설정
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(emailMessage.getTo()); // 수신자 설정
            mimeMessageHelper.setSubject(emailMessage.getSubject()); // 제목 설정
            mimeMessageHelper.setText(emailMessage.getMessage(), isHtml); // 본문 설정 (HTML 여부 지정)

            // 메일 전송 수행
            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailMessage.getTo()); // 성공 로그
            return true;

        } catch (MessagingException e) {
            // 메일 서버 연결 실패, 주소 오류 등 예외 처리
            log.error("Failed to send email to: {}", emailMessage.getTo(), e); // 에러 로그
            return false;
        }
    }
}