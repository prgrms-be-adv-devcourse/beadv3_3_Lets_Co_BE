package co.kr.user.model.DTO.Payment;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentListDTO {
    private PaymentStatus status;
    private PaymentType type;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}