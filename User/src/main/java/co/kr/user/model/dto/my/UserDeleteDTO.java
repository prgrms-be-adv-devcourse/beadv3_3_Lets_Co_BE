package co.kr.user.model.dto.my;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 회원 탈퇴 절차의 1단계(인증 메일 발송)가 성공했을 때,
 * 클라이언트에게 인증 관련 정보를 전달하기 위한 응답 DTO입니다.
 */
@Data
public class UserDeleteDTO {
    /** * 인증 번호가 발송된 사용자의 이메일 주소입니다.
     * 사용자가 어떤 메일로 인증 코드를 확인해야 하는지 안내하는 용도로 사용됩니다.
     */
    private String mail;

    /** * 발송된 인증 번호의 만료 예정 일시입니다.
     * 클라이언트 화면에서 남은 인증 유효 시간을 타이머로 표시할 때 활용됩니다.
     */
    private LocalDateTime certificationTime;
}