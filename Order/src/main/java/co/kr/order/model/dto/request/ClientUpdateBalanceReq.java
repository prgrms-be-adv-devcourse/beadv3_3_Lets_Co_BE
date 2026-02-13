package co.kr.order.model.dto.request;

import java.math.BigDecimal;

public record ClientUpdateBalanceReq(
        Long userIdx,
        BigDecimal amount
) {}
