package co.kr.payment.model.event;

import co.kr.payment.model.dto.UserInfo;

public record PaymentSuccessEvent (
        String orderCode,
        Long paymentIdx,
        UserInfo userInfo
) {}