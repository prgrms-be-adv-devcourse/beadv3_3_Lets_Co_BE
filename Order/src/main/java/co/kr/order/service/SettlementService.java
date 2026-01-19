package co.kr.order.service;

import co.kr.order.model.dto.SettlementInfo;

import java.util.List;

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
    void refundSettlement(Long orderId, Long paymentIdx);

    /*
     * 정산목록 조회를 Order-service 쪽에서 할지, Member-service에서 할지 모르겠어서
     * 내가 생각했을때 member에서 sellerIdx 주면 Order에서 sellerIdx 맞는거 찾아 List나 단건을 주기만 함면 될거같은데
     */
    List<SettlementInfo> getSettlementList(Long sellerIdx);
    SettlementInfo getSettlement(Long sellerIdx, Long paymentIdx);
}
