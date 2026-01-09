package co.kr.product.product.dto.response;

import java.math.BigDecimal;

public class ProductResponse {

    private Long productsIdx;
    private String name;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Long viewCount;

    public ProductResponse(Long productsIdx, String name, BigDecimal price, BigDecimal salePrice, Long viewCount) {
        this.productsIdx = productsIdx;
        this.name = name;
        this.price = price;
        this.salePrice = salePrice;
        this.viewCount = viewCount;
    }

    public Long getProductsIdx() {
        return productsIdx;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getSalePrice() {
        return salePrice;
    }

    public Long getViewCount() {
        return viewCount;
    }
}

