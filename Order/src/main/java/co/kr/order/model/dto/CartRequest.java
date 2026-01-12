package co.kr.order.model.dto;

public record CartRequest(
        Long productIdx,
        Long optionIdx
) { }
