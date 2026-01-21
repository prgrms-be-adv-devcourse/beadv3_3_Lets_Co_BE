package co.kr.user.model.vo;

/**
 * 인증 진행 상태를 정의하는 Enum 클래스입니다.
 * 인증 코드의 유효성 및 처리 단계를 추적하는 데 사용됩니다.
 */
public enum UsersVerificationsStatus {
    /**
     * 대기 중 (인증 코드 발송 후 아직 검증되지 않음)
     */
    PENDING,

    /**
     * 인증 완료 (코드가 일치하여 검증 통과됨)
     */
    VERIFIED,

    /**
     * 만료됨 (유효 시간이 지남)
     */
    EXPIRED,

    /**
     * 실패 (잘못된 코드 입력 등)
     */
    FAILED,

    /**
     * 잠금 (연속 실패 등으로 인한 일시적 인증 제한)
     */
    LOCKED,

    /**
     * 취소됨 (사용자가 인증 과정을 중단함)
     */
    CANCELLED
}