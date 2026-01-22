package co.kr.order.batch.dto;

import lombok.Getter;

import java.math.BigDecimal;

/**
 * 판매자별 월간 정산 집계 DTO
 * - Reader에서 판매자별 합산 금액을 조회
 * - Processor에서 수수료 계산 및 검증 결과 설정
 * - Writer에서 상태 업데이트 여부 결정
 */
@Getter
public class SellerSettlementSummary {

    private final Long sellerIdx;
    private final BigDecimal totalAmount;
    private final Long recordCount;
    private BigDecimal payoutAmount;
    private final boolean validated;
    private final String validationMessage;

    /**
     * JPQL 생성자용 (3개 파라미터)
     */
    public SellerSettlementSummary(Long sellerIdx, BigDecimal totalAmount, Long recordCount) {
        this.sellerIdx = sellerIdx;
        this.totalAmount = totalAmount;
        this.recordCount = recordCount;
        this.validated = false;
        this.validationMessage = null;
    }

    /**
     * 전체 필드 생성자
     */
    public SellerSettlementSummary(Long sellerIdx, BigDecimal totalAmount, Long recordCount,
                                   BigDecimal payoutAmount, boolean validated, String validationMessage) {
        this.sellerIdx = sellerIdx;
        this.totalAmount = totalAmount;
        this.recordCount = recordCount;
        this.payoutAmount = payoutAmount;
        this.validated = validated;
        this.validationMessage = validationMessage;
    }

    /**
     * Processor에서 검증 결과 설정을 위한 복사 생성자
     */
    public SellerSettlementSummary withPayoutResult(BigDecimal payoutAmount, boolean validated, String message) {
        return new SellerSettlementSummary(
                this.sellerIdx,
                this.totalAmount,
                this.recordCount,
                payoutAmount,
                validated,
                message
        );
    }

    @Override
    public String toString() {
        return "SellerSettlementSummary{" +
                "sellerIdx=" + sellerIdx +
                ", totalAmount=" + totalAmount +
                ", recordCount=" + recordCount +
                ", payoutAmount=" + payoutAmount +
                ", validated=" + validated +
                ", validationMessage='" + validationMessage + '\'' +
                '}';
    }
}
