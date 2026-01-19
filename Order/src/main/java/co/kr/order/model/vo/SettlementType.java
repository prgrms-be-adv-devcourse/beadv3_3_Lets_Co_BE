package co.kr.order.model.vo;

/**
 * 정산 상태 VO
 * - SALE: 판매 정산 (주문 완료 시 발생)
 * - REFUND: 환불 차감 (환불 시 발생)
 */
public enum SettlementType {
    Orders_CONFIRMED,
    SETTLE_PAYOUT,
    CANCEL_ADJUST
}
