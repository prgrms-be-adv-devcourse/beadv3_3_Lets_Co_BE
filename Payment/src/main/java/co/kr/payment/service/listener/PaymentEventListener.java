package co.kr.payment.service.listener;

import co.kr.payment.client.OrderClient;
import co.kr.payment.model.event.PaymentSuccessEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventListener {

    private final OrderClient orderClient;

    // TransactionPhase.AFTER_COMMIT: 이전 트랜잭션이 완전히 커밋되고 락이 풀린 직후에 실행
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentSuccess(PaymentSuccessEvent event) {
        try {
            // DB 락이 모두 풀린 아주 안전한 상태에서 Feign 통신 진행
            orderClient.successPayment(event.orderCode(), event.paymentIdx(), event.userInfo());
            log.info("주문 성공 처리(정산/후처리) 완료. orderCode={}", event.orderCode());
        } catch (Exception e) {
            log.error("주문 성공 처리(정산/후처리) 호출 중 실패. orderCode={}", event.orderCode(), e);
            log.error("관리자를 호출해주세요");
        }
    }
}