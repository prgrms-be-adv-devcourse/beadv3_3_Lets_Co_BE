package co.kr.order.model.entity;

import co.kr.order.model.vo.SettlementType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Positive;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정산 내역 엔티티
 * - 주문 완료 시 판매자별 정산 레코드 생성 (Type: ORDERS_CONFIRMED)
 * - 정산 지급 완료 (Type: SETTLE_PAYOUT)
 * - 주문 취소(환불) 시 차감 (Type: CANCEL_ADJUST)
 */
@Entity
@Table(name = "Settlement_History")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SettlementHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Settlement_IDX")
    private Long settlementIdx;

    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    @Column(name = "Payment_IDX", nullable = false)
    private Long paymentIdx;

    /**
     * 정산 유형
     * - ORDERS_CONFIRMED: 주문 완료 (정산 대기)
     * - SETTLE_PAYOUT: 정산 지급 완료
     * - CANCEL_ADJUST: 주문 취소(환불)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 30)
    private SettlementType type;

    @Positive
    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "Created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(columnDefinition = "TINYINT")
    private boolean del;

    @Builder
    public SettlementHistoryEntity(Long sellerIdx, Long paymentIdx, SettlementType type, BigDecimal amount) {
        this.sellerIdx = sellerIdx;
        this.paymentIdx = paymentIdx;
        this.type = type;
        this.amount = amount;
        this.createdAt = LocalDateTime.now();
        this.del = false;
    }

    public void setType(SettlementType type) {
        this.type = type;
    }
}
