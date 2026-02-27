package co.kr.product.common.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class StockScheduler {

    @Scheduled(fixedDelay = 1000 * 60 * 1)
    @Transactional
    public void checkStockInRedis(){

    }
}
