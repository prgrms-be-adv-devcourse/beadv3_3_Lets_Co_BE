package co.kr.product.product.model.dto.response;

import java.util.List;

public record ProductListResponse(
        List<ProductResponse> items
) {

}

