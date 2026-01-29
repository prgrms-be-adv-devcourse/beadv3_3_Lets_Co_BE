package co.kr.payment.model.entity;

import co.kr.payment.model.vo.PaymentStatus;
import co.kr.payment.model.vo.PaymentType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payment")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Payment_IDX")
    private Long paymentIdx;

    @Column(name = "Users_IDX", nullable = false)
    private Long usersIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private PaymentType type;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "Orders_IDX")
    private Long ordersIdx;

    @Column(name = "Card_IDX")
    private Long cardIdx;

    @Column(name = "Payment_Key")
    private String paymentKey;

    @Column(name = "Created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PaymentEntity(
            Long usersIdx,
            PaymentStatus status,
            PaymentType type,
            BigDecimal amount,
            Long ordersIdx,
            Long cardIdx,
            String paymentKey
    ) {
        this.usersIdx = usersIdx;
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.ordersIdx = ordersIdx;
        this.cardIdx = cardIdx;
        this.paymentKey = paymentKey;
        this.createdAt = LocalDateTime.now();
    }

}
