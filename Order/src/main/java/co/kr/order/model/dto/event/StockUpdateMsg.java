package co.kr.order.model.dto.event;

public record StockUpdateMsg (
        String productCode,
        String optionCode,
        Long quantity
) { }
