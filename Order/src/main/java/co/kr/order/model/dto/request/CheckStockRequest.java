package co.kr.order.model.dto.request;

public record CheckStockRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}