package co.kr.user.model.vo;

/**
 * 발급된 인증 코드의 처리 상태를 정의하는 Enum 클래스입니다.
 * 인증의 진행 단계(대기, 완료, 만료 등)를 추적하여 보안 및 유효성 검사에 사용됩니다.
 */
public enum UsersVerificationsStatus {
    /** 인증 대기 중 (코드 발송 후 입력 전) */
    PENDING,
    /** 인증 완료 (코드 일치 확인됨) */
    VERIFIED,
    /** 유효 시간 만료 */
    EXPIRED,
    /** 인증 시도 실패 (코드 불일치) */
    FAILED,
    /** 시도 횟수 초과 등으로 인한 잠금 */
    LOCKED,
    /** 사용자 또는 시스템에 의한 취소 */
    CANCELLED
}