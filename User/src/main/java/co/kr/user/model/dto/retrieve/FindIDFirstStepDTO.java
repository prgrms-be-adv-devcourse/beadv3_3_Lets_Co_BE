package co.kr.user.model.dto.retrieve;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 아이디 찾기 1단계(이메일 확인 및 인증번호 발송) 성공 시 반환되는 응답 DTO입니다.
 */
@Data
public class FindIDFirstStepDTO {
    /** 인증번호가 발송된 사용자의 이메일 주소입니다. */
    private String mail;
    /** 발송된 인증번호의 유효 만료 일시입니다. */
    private LocalDateTime certificationTime;
}