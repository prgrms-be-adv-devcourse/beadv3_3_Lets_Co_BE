package co.kr.order.model.dto;

public record AddCartRequest(
        Long productIdx,
        Long optionIdx
) { }
