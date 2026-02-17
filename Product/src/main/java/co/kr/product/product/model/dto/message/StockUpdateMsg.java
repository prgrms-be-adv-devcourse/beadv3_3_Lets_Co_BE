package co.kr.product.product.model.dto.message;

public record StockUpdateMsg(
        String msgCode,
        String productCode,
        String optionCode,
        Long quantity
) {
}
