package co.kr.product.product.dto.response;

import java.util.List;

public record ProductListResponse(
        String resultCode,
        List<ProductResponse> items
) {

}

