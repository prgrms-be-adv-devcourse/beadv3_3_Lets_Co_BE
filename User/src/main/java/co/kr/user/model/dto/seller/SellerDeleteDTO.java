package co.kr.user.model.dto.seller;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 판매자 탈퇴 절차의 1단계(인증 메일 발송)가 성공했을 때,
 * 클라이언트에게 처리 결과와 인증 관련 정보를 전달하기 위한 응답 DTO입니다.
 */
@Data
public class SellerDeleteDTO {
    /** * 탈퇴를 요청한 판매자의 아이디입니다.
     * 보안을 위해 마스킹 처리가 되어 있거나, 확인 용도로 사용됩니다.
     */
    private String id;

    /** * 본인 확인을 위해 발송된 인증 번호의 생성 또는 만료 예정 일시입니다.
     * 클라이언트 화면에서 남은 인증 시간을 표시하는 데 활용될 수 있습니다.
     */
    private LocalDateTime certificationTime;
}