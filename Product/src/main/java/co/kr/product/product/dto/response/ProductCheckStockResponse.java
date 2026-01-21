package co.kr.product.product.dto.response;

import java.util.List;

public record ProductCheckStockResponse(
        String returnCode,

        Integer productStock,

        List<OptionCheckStockResponse> optionStocks
) {
}
