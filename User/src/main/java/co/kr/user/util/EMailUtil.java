package co.kr.user.util;

import co.kr.user.model.dto.mail.EmailMessage;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * [이메일 발송 유틸리티]
 * JavaMailSender를 사용하여 실제 이메일을 SMTP 서버를 통해 전송하는 클래스입니다.
 * 회원가입 인증 메일, 비밀번호 찾기 메일 등 다양한 곳에서 재사용할 수 있습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EMailUtil {

    // Spring Boot Starter Mail 라이브러리가 제공하는 이메일 전송 인터페이스
    // application.yml에 설정한 mail host, port, username, password 정보를 바탕으로 자동 설정됨
    private final JavaMailSender javaMailSender;

    // 발신자 이메일 주소 (yml 설정 파일에서 가져옴)
    @Value("${spring.mail.username}")
    private String fromEmail;

    // 발신자 이름 (예: "GutJJeu 고객센터")
    // 이메일을 받았을 때 '보낸 사람'에 이메일 주소 대신 표시될 이름
    @Value("${spring.mail.servicename}")
    private String FROM_NAME;

    /**
     * [이메일 전송 메서드]
     * @param emailMessage 수신자, 제목, 본문 내용이 담긴 객체
     * @param isHtml 본문이 HTML 형식인지 여부 (true: HTML 렌더링, false: 단순 텍스트)
     *
     * @Async: 비동기 처리 어노테이션
     * - 이 메서드는 호출 즉시 리턴되며, 실제 메일 발송은 별도의 스레드에서 백그라운드로 실행됩니다.
     * - 장점: 메일 발송이 3~5초 걸리더라도 사용자는 회원가입 버튼을 누르자마자 '완료' 응답을 받을 수 있습니다.
     * - 주의: @EnableAsync가 설정 클래스(UserApplication.java)에 선언되어 있어야 동작합니다.
     */
    @Async
    public void sendEmail(EmailMessage emailMessage, boolean isHtml) {
        // MimeMessage: 파일 첨부나 HTML 등 복잡한 내용을 담을 수 있는 이메일 객체
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            // MimeMessageHelper: MimeMessage를 다루기 쉽게 도와주는 도우미 클래스
            // 두 번째 파라미터 false: 멀티파트(파일 첨부) 모드 미사용 (단순 텍스트/HTML만 보낼 때)
            // 세 번째 파라미터 "UTF-8": 한글 깨짐 방지를 위한 인코딩 설정
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            // 1. [발신자 설정] (이메일 주소, 발신자 이름)
            helper.setFrom(fromEmail, FROM_NAME);

            // 2. [수신자 설정]
            helper.setTo(emailMessage.getTo());

            // 3. [제목 설정]
            helper.setSubject(emailMessage.getSubject());

            // 4. [본문 설정]
            // isHtml이 true이면 <html> 태그가 적용된 예쁜 디자인으로 발송됩니다.
            helper.setText(emailMessage.getMessage(), isHtml);

            // 5. [전송 실행]
            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailMessage.getTo());

        } catch (MessagingException | UnsupportedEncodingException e) {
            // 이메일 발송 실패 시 로그를 남김 (비동기이므로 사용자에게 에러 화면을 보여주진 않음)
            // 운영 환경에서는 관리자에게 알림을 보내거나 재시도(Retry) 로직을 추가하기도 함
            log.error("Failed to send email", e);
        }
    }
}