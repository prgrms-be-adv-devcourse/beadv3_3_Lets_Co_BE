package co.kr.order.model.redis;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor
public class Cart {
    private Long userIdx;
    private Long productIdx;
    private Long optionIdx;
    private String productCode;
    private String optionCode;
    private String productName;
    private String optionContent;
    private BigDecimal price;
    private Integer quantity;

    @Builder
    public Cart (Long userIdx, Long productIdx, Long optionIdx, String productCode, String optionCode, String productName, String optionContent, BigDecimal price, int quantity) {
        this.userIdx = userIdx;
        this.productIdx = productIdx;
        this.optionIdx = optionIdx;
        this.productCode = productCode;
        this.optionCode = optionCode;
        this.productName = productName;
        this.optionContent = optionContent;
        this.price = price;
        this.quantity = quantity;
    }

    /**
     * 수량 개수 증가/감소
     */
    public void plusQuantity() {
        this.quantity = quantity + 1;
    }
    public void minusQuantity() {
        this.quantity = quantity - 1;
    }
}
