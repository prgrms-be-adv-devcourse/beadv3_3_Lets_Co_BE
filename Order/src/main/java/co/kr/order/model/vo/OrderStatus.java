package co.kr.order.model.vo;

/*
 * 주문 상태
 * CREATED: 주문 생성
 * PAID: 결제 완료
 * PAYMENT_FAILED: 결제 실패
 * CANCELLED: 결제 취소
 * REFUNDED: 환불
 * DELIVERING: 배달중
 * ARRIVED: 도착
 * COMPLETED: 완료
 */
public enum OrderStatus {
    CREATED,
    PAID,
    PAYMENT_FAILED,
    CANCELLED,
    REFUNDED,
    DELIVERING,
    ARRIVED,
    COMPLETED
}