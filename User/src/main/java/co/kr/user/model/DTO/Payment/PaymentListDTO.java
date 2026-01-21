package co.kr.user.model.DTO.Payment;

import co.kr.user.model.vo.PaymentStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class PaymentListDTO {
    private PaymentStatus status;
    private BigDecimal amount;
    private LocalDateTime createdAt;
}