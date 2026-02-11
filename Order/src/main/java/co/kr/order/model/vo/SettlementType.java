package co.kr.order.model.vo;

/*
 * ORDERS_CONFIRMED: 주문 완료
 * SETTLE_PAYOUT: 정산 완료
 * CANCEL_ADJUST: 주문 취소(환불)
 */
public enum SettlementType {
    Orders_CONFIRMED,
    SETTLE_PAYOUT,
    CANCEL_ADJUST
}
