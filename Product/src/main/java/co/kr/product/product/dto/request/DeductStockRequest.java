package co.kr.product.product.dto.request;

public record DeductStockRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {
}
