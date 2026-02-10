package co.kr.payment.model.dto.request;

public record RefundReq(
        Long userIdx,
        Long ordersIdx
) {
}
