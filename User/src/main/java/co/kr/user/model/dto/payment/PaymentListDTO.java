package co.kr.user.model.dto.payment;

import co.kr.user.model.vo.PaymentStatus;
import co.kr.user.model.vo.PaymentType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 사용자의 결제 히스토리 화면에서 개별 거래 내역의 상세 정보를 표시하기 위한 DTO입니다.
 */
@Data
public class PaymentListDTO {
    /** 거래의 상태 (결제 완료, 충전 완료, 환불 등) */
    private PaymentStatus status;
    /** 사용된 결제 수단 (CARD, TOSS_PAY 등) */
    private PaymentType type;
    /** 거래된 금액 */
    private BigDecimal amount;
    /** 거래가 발생한 일시 */
    private LocalDateTime createdAt;
}