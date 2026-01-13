package co.kr.order.model.dto.request;

public record OrderRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}
