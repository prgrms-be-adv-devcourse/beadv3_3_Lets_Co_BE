package co.kr.order.queue.config;

import co.kr.order.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@RequiredArgsConstructor
public class WorkerConfig {

    private final QueueService queueService;

    // 1초마다 대기열 유저 입장 처리
    @Scheduled(fixedDelay = 1000)
    public void workerOrder() {
        queueService.allowUser(10);
    }

    // 1분마다 활동 없는 유저 정리 (TTL: 30분)
    @Scheduled(fixedDelay = 60000) // 1분 주기
    public void evictUsers() {
        // 30분 = 30 * 60 * 1000 = 1,800,000 밀리초
        queueService.evictInactiveUsers(1800000L);
    }
}