package co.kr.order.service;

/**
 * 정산 서비스 인터페이스
 */
public interface SettlementService {

    /**
     * 정산 생성 (주문 완료 시 호출)
     * - OrderItem의 상품별 판매자 조회
     * - 판매자별로 정산 레코드 생성 (Type: SALE)
     *
     * @param orderId 주문 ID
     */
    void createSettlement(Long orderId);

    /**
     * 환불 정산 생성 (환불 시 호출)
     * - 환불된 주문의 판매자 조회
     * - 정산 차감 레코드 생성 (Type: REFUND)
     *
     * @param orderId 주문 ID
     * @param paymentIdx 환불 결제 ID
     */
    void createRefundSettlement(Long orderId, Long paymentIdx);
}
