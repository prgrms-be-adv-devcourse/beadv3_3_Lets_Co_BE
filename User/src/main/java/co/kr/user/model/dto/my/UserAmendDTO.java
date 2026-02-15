package co.kr.user.model.dto.my;

import co.kr.user.model.vo.UsersInformationGender;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 회원 정보 수정 요청이 성공적으로 처리된 후,
 * 업데이트된 사용자의 최신 프로필 정보를 담아 반환하는 DTO입니다.
 */
@Data
public class UserAmendDTO {
    /** 수정된 이메일 주소 */
    private String mail;
    /** 수정된 성별 */
    private UsersInformationGender gender;
    /** 수정된 이름 */
    private String name;
    /** 수정된 휴대폰 번호 */
    private String phoneNumber;
    /** 수정된 생년월일 */
    private String birth;
    /** 업데이트된 마케팅 수신 동의 상태 메시지 또는 정보 */
    private LocalDateTime agreeMarketingAt;
}