package co.kr.product.product.model.dto.request;

public record AddStockReq(
        String optionCode,
        Integer stocks
) {
}
