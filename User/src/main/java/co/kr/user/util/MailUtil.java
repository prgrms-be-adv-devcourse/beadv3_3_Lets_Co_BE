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

@Slf4j
@Component
@RequiredArgsConstructor
public class MailUtil {
    private final JavaMailSender javaMailSender;

    @Async
    public void sendEmail(EmailMessage emailMessage, boolean isHtml) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        try {
            MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, false, "UTF-8");

            mimeMessageHelper.setTo(emailMessage.getTo());
            mimeMessageHelper.setSubject(emailMessage.getSubject());
            mimeMessageHelper.setText(emailMessage.getMessage(), isHtml);

            javaMailSender.send(mimeMessage);

            log.info("Email sent successfully to: {}", emailMessage.getTo());

        } catch (MessagingException e) {
            log.error("Failed to send email to: {}", emailMessage.getTo(), e);
        }
    }
}