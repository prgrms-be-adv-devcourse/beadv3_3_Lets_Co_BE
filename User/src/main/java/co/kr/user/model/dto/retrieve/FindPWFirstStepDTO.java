package co.kr.user.model.dto.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 비밀번호 재설정 인증번호 발송 성공 시 반환되는 응답 DTO입니다.
 */
@Data
public class FindPWFirstStepDTO {
    /** 인증번호가 발송된 이메일 주소입니다. */
    private String mail;
    /** 인증번호의 유효 만료 일시입니다. */
    private LocalDateTime certificationTime;
}