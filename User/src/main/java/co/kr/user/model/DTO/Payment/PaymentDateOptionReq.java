package co.kr.user.model.DTO.Payment;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PaymentDateOptionReq {
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}
