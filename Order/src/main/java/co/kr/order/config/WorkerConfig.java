package co.kr.order.config;

import co.kr.order.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

/*
 * 스케쥴러(Worker) 설정
 * - 대기열 시스템
 * -
 */
@Configuration
@RequiredArgsConstructor
public class WorkerConfig {

    private final QueueService queueService;

    /*
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     * 대기열 시스템 Worker
     * ㅡㅡㅡㅡㅡㅡㅡㅡㅡㅡ
     */
    // [입장] 1초마다 10명씩 입장 (유량 제어)
    @Scheduled(fixedDelay = 1000)
    public void workerEnter() {
        queueService.allowEnterUser(10L);
    }

    // [주문] 1초마다 빈 자리(50명 제한) 체크해서 입장
    @Scheduled(fixedDelay = 1000)
    public void workerOrder() {
        // 최대 동시 주문 가능 인원을 50명으로 가정
        queueService.allowOrderUser(50L);
    }

    // [청소] 1분마다 잠수탄 주문 유저 정리
    @Scheduled(fixedDelay = 60000)
    public void evictUsers() {
        // 10분(600,000ms) 동안 반응 없으면 주문 대기열에서 쫓아냄
        long minute = 10L;
        long time = 60000L * minute;
        queueService.evictInactiveUsers(time);
    }
}