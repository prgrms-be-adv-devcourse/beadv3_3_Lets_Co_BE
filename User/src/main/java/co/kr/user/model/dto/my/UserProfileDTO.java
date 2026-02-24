package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자의 상세 프로필(이름, 연락처, 잔액 등) 정보를
 * 화면에 표시하기 위해 사용하는 응답 DTO입니다.
 */
@Data
public class UserProfileDTO {
    /** 사용자 이메일 주소 */
    private String mail;
    /** 성별 (MALE, FEMALE, OTHER) */
    private UsersInformationGender gender;
    /** 현재 보유 중인 잔액/포인트 */
    private BigDecimal balance;
    /** 사용자 실명 */
    private String name;
    /** 연락처(휴대폰 번호) */
    private String phoneNumber;
    /** 생년월일 */
    private String birth;
    /** 마케팅 수신 동의 일시 */
    private LocalDateTime agreeMarketingAt;
}