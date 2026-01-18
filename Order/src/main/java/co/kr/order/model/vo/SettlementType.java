package co.kr.order.model.vo;

/**
 * 정산 유형
 * - SALE: 판매 정산 (주문 완료 시 발생)
 * - REFUND: 환불 차감 (환불 시 발생)
 */
public enum SettlementType {
    SALE,
    REFUND
}
