package co.kr.order.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/*
 * return 할 정산금 정보
 * @param settlementIdx: 정산금 인덱스
 * @param sellerIdx: 판매자 인덱스
 * @param paymentIdx: 결제 인덱스
 * @param type: 정산금 타입 (ORDERS_CONFIRMED, SETTLE_PAYOUT, CANCEL_ADJUST)
 * @param amount: 결제 금액
 * @param createdAt: 생성일
 */
public record SettlementInfo (
        Long settlementIdx,
        Long sellerIdx,
        Long paymentIdx,
        String type,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
