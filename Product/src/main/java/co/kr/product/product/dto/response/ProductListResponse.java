package co.kr.product.product.dto.response;

import java.util.List;

public class ProductListResponse {
    private String resultCode;
    private List<ProductResponse> items;

    public ProductListResponse(
            String resultCode, List<ProductResponse> items) {
        this.resultCode = resultCode;
        this.items = items;
    }

    public String getResultCode() { return resultCode; }
    public List<ProductResponse> getItems() { return items; }
}

