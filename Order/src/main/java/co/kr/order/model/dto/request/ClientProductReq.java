package co.kr.order.model.dto.request;

public record ClientProductReq(
        Long productIdx,
        Long optionIdx
) {}
