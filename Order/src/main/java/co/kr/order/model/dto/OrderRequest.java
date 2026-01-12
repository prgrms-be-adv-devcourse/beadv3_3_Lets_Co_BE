package co.kr.order.model.dto;

public record OrderRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}
