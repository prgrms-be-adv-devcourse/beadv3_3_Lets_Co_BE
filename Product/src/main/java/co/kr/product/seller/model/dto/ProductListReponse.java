package co.kr.product.seller.model.dto;

import java.util.List;

public record ProductListReponse(

        String resultCode,
        List<ProductResponse> items
) {


}
