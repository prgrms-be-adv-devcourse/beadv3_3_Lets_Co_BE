package co.kr.product.product.dto.response;

public record ProductCheckStockResponse(
        String returnCode,

        boolean isInStock
) {
}
