package co.kr.order.model.dto.request;

public record CartRequest(
        Long productIdx,
        Long optionIdx
) { }
