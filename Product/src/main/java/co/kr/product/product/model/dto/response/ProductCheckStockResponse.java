package co.kr.product.product.model.dto.response;

import java.util.List;

public record ProductCheckStockResponse(
        Integer productStock,

        List<OptionCheckStockResponse> optionStocks
) {
}
