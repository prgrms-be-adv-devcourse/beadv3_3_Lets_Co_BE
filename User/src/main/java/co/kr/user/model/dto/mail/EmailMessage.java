package co.kr.user.model.dto.mail;

import lombok.Builder;
import lombok.Data;

/**
 * 메일 전송 서비스(MailUtil 등)에서 실제 메일을 발송하기 위해
 * 수신자, 제목, 본문 내용을 구조화한 데이터 객체입니다.
 */
@Data
@Builder
public class EmailMessage {
    /** 수신자의 이메일 주소 */
    private String to;
    /** 이메일의 제목 */
    private String subject;
    /** 이메일의 본문 내용 (HTML 형식을 포함할 수 있음) */
    private String message;
}