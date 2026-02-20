package co.kr.user.model.dto.seller;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 판매자 입점 신청이 정상적으로 접수되었을 때 반환되는 응답 DTO입니다.
 * 본인 확인을 위한 인증 메일 정보가 포함됩니다.
 */
@Data
public class SellerRegisterDTO {
    /** 인증번호가 발송된 판매자의 이메일 주소입니다. */
    private String mail;
    /** 발급된 인증번호의 유효 만료 일시입니다. */
    private LocalDateTime certificationTime;
}