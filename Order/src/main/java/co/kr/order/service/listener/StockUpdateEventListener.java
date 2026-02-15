package co.kr.order.service.listener;

import co.kr.order.model.dto.event.StockUpdateEvent;
import co.kr.order.service.KafkaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/*
 * 재고 업데이트 이벤트 리스너
 * 주문 로직에서 발행된 이벤트를 받아서 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StockUpdateEventListener {

    private final KafkaService kafkaService;

    /*
     * @param event: eventPublisher.publishEvent()로 넘겨준 이벤트 객체
     *
     * 주문을 생성하는 메인 트랜잭션(DB 저장)이 완전히 Commit 했을 때만 실행
     * 만약 DB 저장 중에 에러가 나서 롤백된다면 이 메서드는 실행되지 않음 (Kafka 메시지가 안 날아감)
     * DB에는 주문이 없는데, Kafka 메시지만 날아가는 상황을 막아줌
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleStockUpdate(StockUpdateEvent event) {
        log.info("Transaction Commit 완료. Kafka 메시지 발행 시작: {}", event.message());

        // Kafka로 메시지 전송
        kafkaService.sendStockUpdate(event.message());
    }
}
