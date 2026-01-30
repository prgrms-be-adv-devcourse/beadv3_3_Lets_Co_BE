package co.kr.product.product.dto.response;

import java.util.List;

public record ProductCheckStockRes(
        String returnCode,

        Integer productStock,

        List<OptionCheckStockRes> optionStocks
) {
}
