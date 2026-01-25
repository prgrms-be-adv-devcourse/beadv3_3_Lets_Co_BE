package co.kr.order.model.entity;

import co.kr.order.model.vo.PaymentStatus;
import co.kr.order.model.vo.PaymentType;
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

    @Column(name = "Orders_IDX")
    private Long ordersIdx;

    @Column(name = "Card_IDX")
    private Long cardIdx;

    @Enumerated(EnumType.STRING)
    @Column(name = "Status", nullable = false, length = 10)
    private PaymentStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "Type", nullable = false, length = 20)
    private PaymentType type;

    @Column(name = "Amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(name = "Payment_Key")
    private String paymentKey;

    @Column(name = "Created_at", nullable = false)
    private LocalDateTime createdAt;

    @Builder
    public PaymentEntity(
            Long usersIdx,
            Long ordersIdx,
            Long cardIdx,
            PaymentStatus status,
            PaymentType type,
            BigDecimal amount,
            String paymentKey
    ) {
        this.usersIdx = usersIdx;
        this.ordersIdx = ordersIdx;
        this.cardIdx = cardIdx;
        this.status = status;
        this.type = type;
        this.amount = amount;
        this.paymentKey = paymentKey;
        this.createdAt = LocalDateTime.now();
    }

}
