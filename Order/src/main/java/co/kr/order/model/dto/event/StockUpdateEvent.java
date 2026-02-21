package co.kr.order.model.dto.event;

/*
 * 이벤트 리스너용 내부 DTO
 * @param message: 재고 감소 이벤트 발행 정보
 */
public record StockUpdateEvent(
        StockUpdateMsg message
) {}
