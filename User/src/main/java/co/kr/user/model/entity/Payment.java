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

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@DynamicInsert
@Table(name = "Payment")
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Payment_IDX", nullable = false)
    private Long paymentIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 20)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private PaymentType type;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    @ColumnDefault("0")
    private BigDecimal amount;

    @CreatedDate
    @Column(name = "Created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "Del", nullable = false)
    @ColumnDefault("0")
    private int del = 0;

    @Builder
    public Payment(Long usersIdx, PaymentStatus status, PaymentType type, BigDecimal amount) {
        this.usersIdx = usersIdx;
        this.status = status;
        this.type = type;
        this.amount = amount;
    }
}