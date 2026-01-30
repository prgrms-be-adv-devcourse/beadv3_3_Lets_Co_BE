package co.kr.product.product.dto.response;

import java.util.List;

public record ProductListRes(
        String resultCode,
        List<ProductRes> items
) {

}

