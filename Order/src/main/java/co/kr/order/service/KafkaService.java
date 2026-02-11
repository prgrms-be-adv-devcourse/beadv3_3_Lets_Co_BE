package co.kr.order.service;

import co.kr.order.model.dto.event.StockUpdateMsg;

public interface KafkaService {
    void sendStockUpdate(StockUpdateMsg stockUpdateMsg);

}
