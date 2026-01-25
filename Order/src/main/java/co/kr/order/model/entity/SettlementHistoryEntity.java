package co.kr.order.model.entity;

import co.kr.order.model.vo.SettlementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 정산 내역 엔티티
 * - 주문 완료 시 판매자별 정산 레코드 생성 (Type: SALE)
 * - 환불 시 차감 레코드 생성 (Type: REFUND)
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

    /**
     * 판매자 ID (Seller 테이블 FK)
     */
    @Column(name = "Seller_IDX", nullable = false)
    private Long sellerIdx;

    /**
     * 결제 ID (Payment 테이블 FK)
     */
    @Column(name = "Payment_IDX", nullable = false)
    private Long paymentIdx;

    /**
     * 정산 유형
     * - SALE: 판매 정산
     * - REFUND: 환불 차감
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 30)
    private SettlementType type;

    /**
     * 정산 금액
     * - SALE: 양수 (판매자에게 지급할 금액)
     * - REFUND: 양수 (환불로 차감할 금액)
     */
    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "Created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
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
