package co.kr.order.model.redis;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class Cart {
    private Long userIdx;
    private Long productIdx;
    private Long optionIdx;
    private Integer quantity;

    @Builder
    public Cart (Long userIdx, Long productIdx, Long optionIdx, int quantity) {
        this.userIdx = userIdx;
        this.productIdx = productIdx;
        this.optionIdx = optionIdx;
        this.quantity = quantity;
    }

    /**
     * 수량 개수 증가/감소
     */
    public void increaseQuantity() {
        this.quantity = quantity + 1;
    }

    public void decreaseQuantity() {
        this.quantity = quantity - 1;
    }
}
