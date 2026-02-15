package co.kr.user.model.dto.register;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 회원가입 1단계(정보 입력) 성공 시, 인증 메일 발송 정보 등을 담아 반환하는 DTO입니다.
 */
@Data
public class RegisterDTO {
    /** 인증 코드가 발송된 이메일 주소 */
    private String mail;
    /** 발송된 인증 코드의 만료 일시 */
    private LocalDateTime certificationTime;
}