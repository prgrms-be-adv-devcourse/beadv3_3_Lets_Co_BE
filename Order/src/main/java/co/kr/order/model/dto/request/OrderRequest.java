package co.kr.order.model.dto.request;

// 주문할 때 필요한 제품id, 옵션id, 주문할 제품 수량
public record OrderRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}
