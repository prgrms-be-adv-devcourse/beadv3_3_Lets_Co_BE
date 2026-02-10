package co.kr.order.service.listener;

import co.kr.order.model.dto.event.StockUpdateEvent;
import co.kr.order.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockUpdateEventListener {

    private final KafkaService kafkaService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockUpdate(StockUpdateEvent event) {
        log.info("Transaction Commit 완료. Kafka 메시지 발행 시작: {}", event.message());

        kafkaService.sendStockUpdate(event.message());
    }
}
