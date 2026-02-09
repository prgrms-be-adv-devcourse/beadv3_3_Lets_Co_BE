package co.kr.order.model.dto.request;

public record ClientRefundReq(
        Long userIdx,
        Long ordersIdx
) {}
