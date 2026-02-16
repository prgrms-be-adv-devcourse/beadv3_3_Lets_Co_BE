package co.kr.user.model.vo;

/**
 * 결제 및 거래의 상태를 정의하는 Enum 클래스입니다.
 * 결제 완료, 충전 완료, 환불 완료 등 거래의 최종 상태를 식별하는 데 사용됩니다.
 */
public enum PaymentStatus {
    /** 상품 또는 서비스 결제 완료 */
    PAYMENT,
    /** 잔액(포인트/머니) 충전 완료 */
    CHARGE,
    /** 결제 취소에 따른 환불 완료 */
    REFUND
}