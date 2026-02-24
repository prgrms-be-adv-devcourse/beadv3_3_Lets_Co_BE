package co.kr.user.model.entity;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자의 결제, 충전, 환불 등의 거래 내역을 관리하는 Entity 클래스입니다.
 * 모든 금전적 흐름을 기록하며, 수정이나 삭제 없이 이력 저장 목적으로 주로 사용됩니다.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "Payment")
public class Payment {
    /** 결제 내역 고유 식별자 (PK) */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Payment_IDX", nullable = false)
    private Long paymentIdx;

    /** 거래가 발생한 사용자의 식별자 */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /** 거래 상태 (PAYMENT: 결제 완료, CHARGE: 충전 완료, REFUND: 환불 완료) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 30)
    private PaymentStatus status;

    /** 결제 수단 유형 (CARD: 카드, DEPOSIT: 무통장, TOSS_PAY: 토스페이 등) */
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private PaymentType type;

    /** 거래 금액 (정밀한 금액 계산을 위해 BigDecimal 사용) */
    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    /** 거래 발생 일시 */
    @Column(name = "Created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}