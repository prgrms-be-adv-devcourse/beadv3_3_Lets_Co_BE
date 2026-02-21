package co.kr.order.model.dto;

/**
 * 재고 감소 후 남은 재고 정보
 * @param optionCode: 제품 옵션 코드
 * @param quantity: 남은 개수
 */
public record RemainStockInfo (
        String optionCode,
        Long quantity
) {}
