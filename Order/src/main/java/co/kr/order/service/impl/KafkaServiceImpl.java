package co.kr.order.service.impl;

import co.kr.order.model.dto.event.StockUpdateMsg;
import co.kr.order.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "product-stock-update";  // topic

    public void sendStockUpdate(StockUpdateMsg message) {
        log.info("Kafka 메시지 발행 시작: {}", message);

        // send(토픽, 키, 메시지)
        // 키를 productCode로 주면, 같은 상품은 같은 파티션으로 가서 순서가 보장
        kafkaTemplate.send(TOPIC, message.productCode(), message);
    }
}