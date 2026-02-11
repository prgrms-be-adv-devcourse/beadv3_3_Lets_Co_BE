package co.kr.payment.model.dto.request;

import co.kr.payment.model.vo.PaymentStatus;

import java.math.BigDecimal;

public record BalanceClientReq(
        Long userIdx,
        PaymentStatus status,
        BigDecimal balance
) {
}
