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

    /*
     * Lua Script 정의
     * Redis는 싱글 스레드로 동작하므로, Lua Script 내부의 로직은 원자적(Atomic)으로 실행
     * 즉, 재고 조회와 차감 사이에 다른 스레드가 끼어들 수 없음 (동시성 문재 해결)
     *
     * 로직:
     * 1. 현재 재고 조회 (KEYS[1])
     * 2. 재고가 없으면(nil) -1 반환
     * 3. 현재 재고 >= 요청 수량(ARGV[1]) 이면 차감 수행 후 남은 재고 반환
     * 4. 아니면(재고 부족) -1 반환
     */
    private static final String DECREASE_STOCK_SCRIPT =
            "local stock = tonumber(redis.call('get', KEYS[1])) " +
                    "if stock == nil then return -1 end " +
                    "if stock >= tonumber(ARGV[1]) then " +
                    "    return redis.call('decrby', KEYS[1], ARGV[1]) " +
                    "else " +
                    "    return -1 " +
                    "end";

    // 스크립트 실행 객체 (싱글톤으로 관리하여 성능 최적화)
    private DefaultRedisScript<Long> redisScript;

    /*
     * 빈 생성 시 Lua Script 초기화
     * 매 요청마다 스크립트 객체를 생성하지 않고 재사용
     */
    @PostConstruct
    public void init() {
        // Redis에서 사용할 스크립트 객체 생성
        redisScript = new DefaultRedisScript<>();
        // 실행할 Lua 스크립트 설정
        redisScript.setScriptText(DECREASE_STOCK_SCRIPT);
        // Lua 스크립트 실행 후 반환되는 값의 타입 설정
        redisScript.setResultType(Long.class);

        log.info("Redis Script Initialized");
    }

    /**
     * Redis의 상품 재고 감소
     * @param optionCode: 주문한 상품 옵션 코드
     * @param quantity: 주문 개수
     */
    @Override
    public Long decreaseStock(String optionCode, int quantity) {

        // 재고 Key 세팅
        String key = STOCK_KEY + optionCode;

//        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
//        redisScript.setScriptText(DECREASE_STOCK_SCRIPT);
//        redisScript.setResultType(Long.class);

        // execute()를 통해 스크립트 실행 (Key List, ARGV List 전달)
        Long stock = redisTemplate.execute(
                redisScript,
                Collections.singletonList(key),
                String.valueOf(quantity)
        );

        // -1이면 재고 부족
        if (stock < 0) {
            // 재고 부족하면 OutOfStockException
            log.info("재고가 부족, code: {}", optionCode);
            throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
        }

        // 남은 재고 return
        return stock;
    }

    /*
     * 다중 상품 재고 차감 (장바구니)
     * @param request: 주문한 상품들 정보
     */
    @Override
    public List<RemainStockInfo> decreaseStocks(List<ProductInfo> request) {

        // 상품 별 남은 재고량 저장 (return 용)
        List<RemainStockInfo> remainStock = new ArrayList<>();

        // 재고 감소에 성공한 상품
        List<ProductInfo> success = new ArrayList<>();
        // redis에 저장된 상품 재고
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        try {
            // 반복문 돌면서 재고 차감 진행
            for (ProductInfo item : request) {

                // 재고 Key 세팅
                String key = STOCK_KEY + item.optionCode();

                // 상품 재고 주문 개수만큼 감소
                Long stock = ops.decrement(key, item.quantity());

                // 알 수 없는 오류 (null이 될일이 없음)
                if (stock == null) {
                    throw new RuntimeException("재고 감소 중 알수없는 오류");
                }

                // 재고 부족 시
                if (stock < 0) {

                    // 재고 감소에 성공한 상품 다시 롤백(직접 increment) 후 OutOfStockException
                    log.info("장바구니 재고 부족 발생. 즉시 복구 후 롤백 진입. Item: {}", item.optionCode());
                    ops.increment(key, item.quantity());

                    throw new OutOfStockException(ErrorCode.OUT_OF_STOCK);
                }

                // 성공한 상품 재고 세팅
                success.add(item);
                remainStock.add(
                        new RemainStockInfo (
                                item.optionCode(),
                                stock
                        )
                );
            }
        } catch (RuntimeException e) {

            // 재고 차감 중 오류 발생 시
            log.error("재고 차감 중 오류 발생! 전체 롤백 수행. Error: {}", e.getMessage());

            try {
                // 재고 차감이 진행된 (성공한) 상품 다시 rollback
                for (ProductInfo item : success) {
                    String key = STOCK_KEY + item.optionCode();
                    // 직접 increment
                    ops.increment(key, item.quantity());
                }
            } catch (RuntimeException rollback) {
                // 만약 위의 try 작업중 한번더 터지면 정말 문제. 리자가 직접 처리해야함
                log.error("CRITICAL: 롤백 중 2차 오류. 관리자 개입 필요: {}", rollback.getMessage());
            }

            throw e;
        }

        return remainStock;
    }

    /*
     * 단일 상품 롤백 (결제 실패 시)
     * @param optionCode: 상품 옵션 코드
     * @param quantity: 상품 주문 개수
     */
    @Override
    public void rollBackStock(String optionCode, int quantity) {
        String key = STOCK_KEY + optionCode;
        ValueOperations<String, String> ops = redisTemplate.opsForValue();
        ops.increment(key, quantity);
    }

    /*
     * 상품 리스트 롤백 (결제 실패 시)
     * @param request: 주문 상품 정보
     */
    @Override
    public void rollBackStocks(List<ProductInfo> request) {

        log.error("결제 실패로 인한 재고 복구");

        // redis에 저장된 상품 재고
        ValueOperations<String, String> ops = redisTemplate.opsForValue();

        try {
            // 반복문 돌리면서 rollback
            for (ProductInfo item : request) {
                String key = STOCK_KEY + item.optionCode();
                // 직접 increment
                ops.increment(key, item.quantity());
            }
        } catch (RuntimeException rollback) {

            // 만약 위의 try 작업중 한번더 터지면 정말 문제. 관리자가 직접 처리해야함
            log.info("RollBack 중 오류. 관리자 호출 요망 (정말 심각함): {}", rollback.getMessage());
        }
    }
}
