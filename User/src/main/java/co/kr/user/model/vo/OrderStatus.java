package co.kr.user.model.vo;

/**
 * 주문 및 거래의 처리 상태를 정의하는 Enum 클래스입니다.
 * 사용자의 잔액 변경(결제, 충전, 환불) 시 해당 거래의 성격을 나타냅니다.
 */
public enum OrderStatus {
    /** 결제 완료 상태 */
    PAYMENT,
    /** 포인트/예치금 충전 상태 */
    CHARGE,
    /** 환불 처리 상태 */
    REFUND
}