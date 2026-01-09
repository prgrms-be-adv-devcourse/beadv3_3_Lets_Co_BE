package co.kr.product.product.dto.response;

import java.math.BigDecimal;
import java.util.List;

public class ProductDetailResponse {

    private Long productsIdx;

    private String name;
    private String description;

    private BigDecimal price;
    private BigDecimal salePrice;

    private Integer stock;
    private String status;

    private Long viewCount;

    private List<ProductImageResponse> images;
    private List<ProductOptionResponse> options;

    public ProductDetailResponse(
            Long productsIdx,
            String name,
            String description,
            BigDecimal price,
            BigDecimal salePrice,
            Integer stock,
            String status,
            Long viewCount,
            List<ProductImageResponse> images,
            List<ProductOptionResponse> options
    ) {
        this.productsIdx = productsIdx;
        this.name = name;
        this.description = description;
        this.price = price;
        this.salePrice = salePrice;
        this.stock = stock;
        this.status = status;
        this.viewCount = viewCount;
        this.images = images;
        this.options = options;
    }

    public Long getProductsIdx() { return productsIdx; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public BigDecimal getSalePrice() { return salePrice; }
    public Integer getStock() { return stock; }
    public String getStatus() { return status; }
    public Long getViewCount() { return viewCount; }
    public List<ProductImageResponse> getImages() { return images; }
    public List<ProductOptionResponse> getOptions() { return options; }
}



