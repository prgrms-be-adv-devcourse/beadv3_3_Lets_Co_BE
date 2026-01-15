package co.kr.order.model.dto.request;

/*
 * @param productIdx
 * @param optionIdx
 * @param quantity
 * 주문 시 상품 서비스에게 quantity 전송 (재고관리)
 */
public record CheckStockRequest(
        Long productIdx,
        Long optionIdx,
        Integer quantity
) {}