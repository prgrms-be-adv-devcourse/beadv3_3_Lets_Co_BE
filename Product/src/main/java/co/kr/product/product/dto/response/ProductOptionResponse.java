package co.kr.product.product.dto.response;

import java.math.BigDecimal;

public class ProductOptionResponse {

    private Long optionGroupIdx;
    private String optionName;

    private BigDecimal optionPrice;
    private BigDecimal optionSalePrice;

    private Integer stock;
    private String status;

    public ProductOptionResponse(
            Long optionGroupIdx,
            String optionName,
            BigDecimal optionPrice,
            BigDecimal optionSalePrice,
            Integer stock,
            String status
    ) {
        this.optionGroupIdx = optionGroupIdx;
        this.optionName = optionName;
        this.optionPrice = optionPrice;
        this.optionSalePrice = optionSalePrice;
        this.stock = stock;
        this.status = status;
    }

    public Long getOptionGroupIdx() { return optionGroupIdx; }
    public String getOptionName() { return optionName; }
    public BigDecimal getOptionPrice() { return optionPrice; }
    public BigDecimal getOptionSalePrice() { return optionSalePrice; }
    public Integer getStock() { return stock; }
    public String getStatus() { return status; }
}

