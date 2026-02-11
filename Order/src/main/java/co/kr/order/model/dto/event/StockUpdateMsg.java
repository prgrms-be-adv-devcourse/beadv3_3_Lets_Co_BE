package co.kr.order.model.dto.event;

/*
 * 재고 감소 이벤트 발행 정보
 * @param productCode: 제품 코드
 * @param optionCode: 제품 옵션 코드
 * @param quantity: 감소할 개수
 */
public record StockUpdateMsg (
        String productCode,
        String optionCode,
        Long quantity
) {}
