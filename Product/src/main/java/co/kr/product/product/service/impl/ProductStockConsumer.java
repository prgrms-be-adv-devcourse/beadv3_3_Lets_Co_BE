package co.kr.product.product.service.impl;


import co.kr.product.product.model.dto.message.StockUpdateMsg;
import co.kr.product.product.model.entity.ProductEntity;
import co.kr.product.product.model.entity.ProductOptionEntity;
import co.kr.product.product.repository.ProductOptionRepository;
import co.kr.product.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProductStockConsumer {

    private final JdbcTemplate jdbcTemplate;
    private final StringRedisTemplate stringRedisTemplate;
    private final ProductOptionRepository productOptionRepository;

    @Value("${custom.redis.product-stock-key}")
    private String PRODUCT_STOCK_KEY;

    private static final String MSG_HISTORY_KEY = "msg:history:";

    /**
     * 상품 재고 메시지 수신 및 처리
     * 1. kafka 메시지를 소비하여 db에 현 재고수 최신화
     * 2. redis 확인 후 redis의 재고수가 db보다 많으면 문제 있다고 판단.
     * 3. 문제 있을 시 db의 재고수를 redis에 덮어씌움
     * ※ DB > redis는 정상적인 흐름이라 판단
     * ※ DB의 재고는 kafka 메시지를 통해 받은 데이터만 처리하므로 최신화가 느릴지언정,
     *   문제있는 값을 가지고 있을 경우는 없다고 판단
     * @param messages
     */
    @KafkaListener(topics = "${custom.kafka.topic.product-stock.event}")
    @Transactional
    public void messageConsumer(List<StockUpdateMsg> messages){

        // 0. 받은 메시지에 대해 중복으로 온 메시지가 있나 확인
        List<StockUpdateMsg> uniqueMsg = filterDuplicates(messages);

        // 1. 받은 메시지 소비, optionCode 기준으로 각 상품 별 판매량 묶기.
        Map<String, Long> orderMap = uniqueMsg.stream()
                // groupingBy 특정 기준으로 데이터를 묶에 Map으로 반환
                // 1.groupingBy(key) > key 기준으로 묶어서 Map<key, List<~~>> 반환
                // 2. groupingBy(key, downstream) > key 기준으로 묶어 값에 계산된 결과를 넣음
                .collect(Collectors.groupingBy(
                        StockUpdateMsg::optionCode,
                        Collectors.summingLong(StockUpdateMsg::quantity)
                ));

        // 1.1 싱글스레드 kafka면 문제가 없는데, 멀티 스레드 설정 시  여러개의 배치가 동시 실행 될 수 도 있음
        //      이때 리스트로 정렬을 해두면 데드락의 가능성을 막을 수 있다고 함.
        List<Map.Entry<String, Long>> sortedUpdates = new ArrayList<>(orderMap.entrySet());
        sortedUpdates.sort(Map.Entry.comparingByKey());

        log.info("kafka 조회 성공 : "+sortedUpdates);

        // 2. redis 조회
        // 2.1 keys 생성
        List<String> keys = sortedUpdates.stream()
                .map(entry -> PRODUCT_STOCK_KEY + entry.getKey())
                .toList();

        // 2.2 multiGet을 통한 조회
        List<String> redisResult = stringRedisTemplate.opsForValue().multiGet(keys);

        log.info("redis 조회 성공 : "+redisResult);


        // 3. jdbcTemplate.batchUpdate를 이용하여 메시지 내역을 바탕으로 db에 저장
        List<String> optionCodes = batchUpdateStock(sortedUpdates);

        // 4. 수정 된 재고 조회 및 redis와 비교
        // 4.1 조회
        List<ProductOptionEntity> options = productOptionRepository.findByOptionCodeInAndDelFalse(optionCodes);

        // 4.2 비교를 위해 위에서 찾은 options를 Map 으로 변환
        Map<String, ProductOptionEntity> optionsMap = options.stream()
                .collect(Collectors.toMap(
                        ProductOptionEntity::getOptionCode,
                        Function.identity()
                ));

        // 4.3 비교 및 비정상 데이터 보정
        stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            RedisSerializer<String> stringSerializer = stringRedisTemplate.getStringSerializer();

            // sortedUpdates와 redisResult의 순서는 동일
            for(int i = 0; i < sortedUpdates.size(); i++){
                String optionCode = sortedUpdates.get(i).getKey();
                Long redisVal = Long.parseLong(redisResult.get(i));

                ProductOptionEntity dbEntity = optionsMap.get(optionCode);

                // redis의 재고가 db의 재고보다 많을 경우 비정상적인 상황.
                // 이때 db의 재고를 redis 재고에 덮어 씌움.
                if (redisVal > dbEntity.getStock()){
                    log.warn("재고 이상 : OptionCode={}, Redis={}, DB={}",
                            optionCode,redisVal, dbEntity.getStock());

                    // redis에 적용
                    String key = PRODUCT_STOCK_KEY + optionCode;

                    try{
                        byte[] keyBytes = stringSerializer.serialize(key);
                        byte[] valueBytes = stringSerializer.serialize(String.valueOf(dbEntity.getStock()));

                        connection.stringCommands().set(keyBytes, valueBytes);
                    }catch (IllegalArgumentException e) {
                        log.error("Redis 재고 덮어쓰기 에러. OptionCode={}", optionCode, e);}
                }
            }

            return null;
        });

    }

    private List<String> batchUpdateStock(List<Map.Entry<String, Long>> updates) {
        String sql = "UPDATE Products " +
                "SET Stock = Stock - ? " +
                "WHERE Products_Code = ? " +
                "AND Stock >= ? ";

        int globalIndex = 0; // 원본 리스트(updates)의 인덱스를 추적하기 위함

        int[][] results = jdbcTemplate.batchUpdate(sql, updates, updates.size(),
                (ps, entry) -> {
                    ps.setLong(1, entry.getValue()); // 총 주문 수
                    ps.setString(2, entry.getKey());   // optionCode
                    ps.setLong(3, entry.getValue());
                });

        for(int[] row : results){
            for(int result : row){
                // result: 업데이트 된 행. 즉 1이면 성공, 0이면 실패
                if (result == 0){
                    log.error("상품 재고 db에 적용 실패. 옵션 코드 : " + updates.get(globalIndex));
                }
                globalIndex ++ ;

            }
        }

        log.info("db 적용 성공");
        return  updates.stream()
                .map(Map.Entry::getKey)
                .toList();

    }

    /**
     * msgCode 기준 중복 된 메시지를 제거 (redis 이용)
     * @param messages
     * @return
     */
    private List<StockUpdateMsg> filterDuplicates(List<StockUpdateMsg> messages) {
        // 1. Redis Pipeline 실행 (결과 : true/false 리스트)
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();

            for (StockUpdateMsg msg : messages) {

                // key를 msgCode로 설정
                String key = MSG_HISTORY_KEY + msg.msgCode();
                byte[] keyBytes = serializer.serialize(key);
                byte[] valueBytes = serializer.serialize("1");


                // (30분 동안 유효, 존재하지 않을 때만 Set)
                connection.stringCommands().set(
                        keyBytes,
                        valueBytes,
                        // TTL 30분 (재시도/중복 윈도우 커버)
                        Expiration.seconds(1800),
                        // key가 없는 경우에만 set 하는 옵션
                        RedisStringCommands.SetOption.ifAbsent()
                );
            }
            return null;
        });

        // 2. 결과가 true(성공)인 메시지인 것만 필터링
        List<StockUpdateMsg> uniqueMessages = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++) {
            Boolean isSuccess = (Boolean) results.get(i);
            
            if (isSuccess != null && isSuccess) {
                uniqueMessages.add(messages.get(i));
            }
        }

        return uniqueMessages;
    }
}
