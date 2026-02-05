package co.kr.product.product.model.dto.response;

import java.util.List;

public record ProductCheckStockRes(
        Integer productStock,

        List<OptionCheckStockRes> optionStocks
) {
}
