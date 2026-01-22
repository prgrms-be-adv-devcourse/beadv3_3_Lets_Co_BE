package co.kr.order.model.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SettlementInfo (
        Long settleIdx,
        Long sellerIdx,
        Long paymentIdx,
        String type,
        BigDecimal amount,
        LocalDateTime createdAt
) {}
