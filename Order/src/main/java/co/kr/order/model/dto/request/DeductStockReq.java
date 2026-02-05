package co.kr.order.model.dto.request;

public record DeductStockReq(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}