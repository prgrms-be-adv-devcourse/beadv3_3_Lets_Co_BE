package co.kr.user.model.entity;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.DynamicInsert;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자의 결제 및 포인트 충전 내역을 관리하는 엔티티 클래스입니다.
 * 자금의 흐름(충전, 사용, 환불 등)을 기록하여 이력을 추적하고 정산을 지원합니다.
 */
@Entity // JPA 엔티티임을 명시합니다.
@Getter // 모든 필드의 Getter 메서드를 자동 생성합니다.
@NoArgsConstructor(access = AccessLevel.PROTECTED) // 기본 생성자의 접근을 PROTECTED로 제한합니다.
@EntityListeners(AuditingEntityListener.class) // 생성일(createdAt) 자동 관리를 위해 Auditing 기능을 활성화합니다.
@DynamicInsert // INSERT 시 null인 필드를 제외하여 DB Default 값이 적용되도록 합니다.
@Table(name = "Payment") // 데이터베이스의 "Payment" 테이블과 매핑됩니다.
public class Payment {
    /**
     * 결제 내역의 고유 식별자(PK)입니다.
     * 데이터베이스의 Auto Increment 값을 사용합니다.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Payment_IDX", nullable = false)
    private Long paymentIdx;

    /**
     * 결제 또는 충전을 수행한 사용자의 식별자(FK)입니다.
     * Users 테이블의 PK를 참조합니다.
     */
    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    /**
     * 결제 상태를 나타냅니다.
     * 예: PAYMENT(결제 완료), CHARGE(충전 완료), REFUND(환불) 등
     * Enum 값을 문자열로 저장합니다.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private PaymentStatus status;

    /**
     * 결제 수단 또는 유형을 나타냅니다.
     * 예: CARD(카드), DEPOSIT(무통장 입금), TOSS_PAY(토스페이) 등
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private PaymentType type;

    /**
     * 결제 또는 충전 금액입니다.
     * 금융 데이터의 정확성을 위해 BigDecimal 타입을 사용하며, 소수점 2자리까지 저장합니다.
     */
    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal amount;

    /**
     * 결제 발생 일시입니다.
     * JPA Auditing에 의해 데이터 생성 시 자동으로 기록됩니다.
     */
    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * 내역 삭제 여부를 나타내는 플래그입니다.
     * 0: 정상, 1: 삭제됨 (Soft Delete)
     * 결제 내역은 중요 데이터이므로 보통 삭제하지 않으나, 관리 목적상 필드가 존재합니다.
     */
    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    /**
     * 결제 내역 생성을 위한 빌더입니다.
     *
     * @param usersIdx 사용자 식별자
     * @param status 결제 상태
     * @param type 결제 유형
     * @param amount 금액
     */
    @Builder
    public Payment(Long usersIdx, PaymentStatus status, PaymentType type, BigDecimal amount) {
        this.usersIdx = usersIdx;
        this.status = status;
        this.type = type;
        this.amount = amount;
    }
}