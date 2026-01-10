package co.kr.order.domain.model.dto;

public record CartRequest(
        Long productIdx,
        Long optionIdx
) { }
