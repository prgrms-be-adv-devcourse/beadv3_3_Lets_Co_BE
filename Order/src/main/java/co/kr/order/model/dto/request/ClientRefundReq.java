package co.kr.order.model.dto.request;

public record ClientRefundReq(
        Long userIdx,
        String orderCode
) {}
