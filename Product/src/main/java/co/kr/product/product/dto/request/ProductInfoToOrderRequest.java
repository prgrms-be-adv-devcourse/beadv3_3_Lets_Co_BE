package co.kr.product.product.dto.request;

public record ProductInfoToOrderRequest(
        Long productIdx,
        Long optionIdx
) {

}
