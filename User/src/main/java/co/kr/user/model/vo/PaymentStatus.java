package co.kr.user.model.vo;

/**
 * 결제 및 거래 상태를 정의하는 Enum 클래스입니다.
 * 자금 흐름의 성격(구매, 충전, 환불)을 구분합니다.
 */
public enum PaymentStatus {
    /**
     * 결제 완료 (상품 구매 등으로 인한 차감)
     */
    PAYMENT,

    /**
     * 충전 완료 (예치금/포인트 충전)
     */
    CHARGE,

    /**
     * 환불 완료 (결제 취소 또는 반품 처리)
     */
    REFUND
}