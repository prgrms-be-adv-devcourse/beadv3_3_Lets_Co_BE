package co.kr.order.service.impl;

import co.kr.order.exception.ErrorCode;
import co.kr.order.exception.OutOfStockException;
import co.kr.order.model.dto.ProductInfo;
import co.kr.order.model.dto.RemainStockInfo;
import co.kr.order.service.DeductStockService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeductStockServiceImpl implements DeductStockService {

    private final StringRedisTemplate redisTemplate;
    private final static String STOCK_KEY = "product:stock:";

    // 재고(KEYS[1])를 가져와서, 주문량(ARGV[1])보다 크거나 같으면 줄이고(DECRBY), 아니면 -1을 리턴
    private static final String DECREASE_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +
                    "if stock == nil then return -1 end " +
                    "if stock >= tonumber(ARGV[1]) then " +
                    "    return redis.call('decrby', KEYS[1], ARGV[1]) " +
                    "else " +
                    "    return -1 " +
                    "end";

    // 싱글톤을 위해
    private DefaultRedisScript<Long> redisScript;

    @PostConstruct
    public void init() {
        redisScript = new DefaultRedisScript<>();
        redisScript.setScriptText(DECREASE_STOCK_SCRIPT);
        redisScript.setResultType(Long.class);

        log.info("Redis Script Initialized");
    }

    @Override
    public Long decreaseStock(String optionCode, int quantity) {

        String key = STOCK_KEY + optionCode;

//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        redisScript.setScriptText(DECREASE_STOCK_SCRIPT);
//        redisScript.setResultType(Long.class);

        Long stock = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(quantity)
        );

        // -1이면 재고 부족
        if (stock < 0) {
            log.info("재고가 부족, code: {}", optionCode);
            throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
        }

        return stock;
    }

    @Override
    public List<RemainStockInfo> decreaseStocks(List<ProductInfo> request) {

        List<RemainStockInfo> remainStock = new ArrayList<>();

        List<ProductInfo> success = new ArrayList<>();
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        try {
            for (ProductInfo item : request) {
                String key = STOCK_KEY + item.optionCode();

                Long stock = ops.decrement(key, item.quantity());

                if (stock == null) {
                    throw new RuntimeException("재고 감소 중 알수없는 오류");
                }

                if (stock < 0) {
                    log.info("장바구니 재고 부족 발생. 즉시 복구 후 롤백 진입. Item: {}", item.optionCode());
                    ops.increment(key, item.quantity());

                    throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
                }

                success.add(item);
                remainStock.add(
                        new RemainStockInfo (
                                item.optionCode(),
                                stock
                        )
                );
            }
        } catch (RuntimeException e) {

            log.error("재고 차감 중 오류 발생! 전체 롤백 수행. Error: {}", e.getMessage());

            try {
                for (ProductInfo item : success) {
                    String key = STOCK_KEY + item.optionCode();
                    ops.increment(key, item.quantity());
                }
            } catch (RuntimeException rollback) {
                log.error("CRITICAL: 롤백 중 2차 오류. 관리자 개입 필요: {}", rollback.getMessage());
            }

            throw e;
        }

        return remainStock;
    }

    @Override
    public void rollBackStock(String optionCode, int quantity) {
        String key = STOCK_KEY + optionCode;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.increment(key, quantity);
    }

    @Override
    public void rollBackStocks(List<ProductInfo> request) {

        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        log.error("결제 실패로 인한 재고 복구");
        try {
            for (ProductInfo item : request) {
                String key = STOCK_KEY + item.optionCode();
                ops.increment(key, item.quantity());
            }
        } catch (RuntimeException rollback) {
            log.info("RollBack 중 오류. 관리자 호출 요망 (정말 심각함): {}", rollback.getMessage());
        }
    }
}
