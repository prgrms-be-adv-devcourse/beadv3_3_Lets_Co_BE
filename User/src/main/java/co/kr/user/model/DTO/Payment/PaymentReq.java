package co.kr.user.model.DTO.Payment;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class PaymentReq {
    private List<PaymentStatus> paymentStatus;
    private List<PaymentType> paymentType;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}