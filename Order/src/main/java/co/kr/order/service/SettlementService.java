package co.kr.order.service;

import co.kr.order.model.dto.SettlementInfo;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 정산 서비스 인터페이스
 */
public interface SettlementService {


    /**
     * 주문 확정 시 판매자별 정산 레코드 생성 (ORDERS_CONFIRMED)
     *
     * @param paymentIdx 결제 ID
     * @param sellerSettlementMap 판매자별 정산 금액 (sellerIdx → amount)
     */
    void createSettlement(Long paymentIdx, Map<Long, BigDecimal> sellerSettlementMap);

    /**
     * 환불 정산 처리 (환불 시 호출)
     * - 환불된 주문의 판매자 조회
     * - 기존 정산 레코드(ORDERS_CONFIRMED) 조회
     * - 지급 완료가 아니면 CANCEL_ADJUST로 상태 변경
     *
     * @param orderId 주문 ID
     * @param paymentIdx 환불 결제 ID
     */
    void refundSettlement(Long orderId, Long paymentIdx);

    /**
     * 판매자별 정산 내역 목록 조회
     */
    List<SettlementInfo> getSettlementList(Long sellerIdx);

    /**
     * 판매자의 특정 결제 건 정산 상세 조회
     */
    SettlementInfo getSettlement(Long sellerIdx, Long paymentIdx);
}
