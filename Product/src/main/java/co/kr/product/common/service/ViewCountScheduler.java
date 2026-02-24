package co.kr.product.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class ViewCountScheduler {

    @Value("${custom.redis.product-view-count-key}")
    private String viewCountKey;

    @Value("${custom.redis.product-view-dirty-key}")
    private String dirtyKey;
    
    private final StringRedisTemplate stringRedisTemplate;

    private final JdbcTemplate jdbcTemplate;

    @Scheduled(fixedDelay = 1000 * 60 * 1)
    @Transactional
    public void setViewCount(){

        /**
         * opsForSet().
         * .members(key) : 해당 key의 값을 다 가져옴
         * .isMember(key, val) : 해당  key에 들어있음?
         * .scan(key, options) : 데이터를 나눠서 페이징처리해서 가져옴
         */

        // 1. 조회 된 상품 목록(조회수가 변경 된)
        Set<String> viewedProductIdx = stringRedisTemplate.opsForSet().members(dirtyKey);


        // 1.1 유효성 검사
        if (viewedProductIdx == null||viewedProductIdx.isEmpty()){
            return;
        }

        List<String> productsIdxList = new ArrayList<>(viewedProductIdx);

        // 2. 요청
        // redis 요청을 한건 한건 보내는 것이 아닌, 모두 모아서 한번에 보냄
        // 기존 메서드는 기본적으로 커넥션을 열고 닫기에 부적합. 로우 레벨의 connection을 가져와서 사용
        List<Object> results = stringRedisTemplate.executePipelined((RedisCallback<Object>) connection -> {

            for (String ProductIdxStr : productsIdxList) {
                String key = viewCountKey + ProductIdxStr;
                RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();

                // getDel로 원자단위에서 처리
                connection.stringCommands().getDel(serializer.serialize(key));

            }
            return null;
        });


        // 3. idx 와 조회 결과 매핑

        Map<Long, Long> updateMap = new HashMap<>();
        RedisSerializer<String> serializer = stringRedisTemplate.getStringSerializer();

        for (int i = 0; i < productsIdxList.size(); i++) {
            Object rawValue = results.get(i);                // i번째 결과는
            String productIdxStr = productsIdxList.get(i); // i번째 상품의 것이다.

            String valStr = null;
            if ( rawValue instanceof byte[]) {
                valStr = serializer.deserialize((byte[]) rawValue);
                updateMap.put(Long.parseLong(productIdxStr), Long.parseLong(valStr));
            }
            else if (rawValue != null){
                valStr = String.valueOf(rawValue);
                updateMap.put(Long.parseLong(productIdxStr), Long.parseLong(valStr));
            }


        }

        // 4. db에 반영
        if (!updateMap.isEmpty()) {
            batchUpdateViewCounts(updateMap);
        }

        // 5. 처리 된 idx를 항목에서 제거
        // 전부 삭제하는 것이 아닌, 이번에 업데이트 된 항목만 제거
        stringRedisTemplate.opsForSet().remove(dirtyKey ,productsIdxList.toArray(new String[0]));

    }

    // JDBC Batch Update
    // JPA의 saveAll 보다 db의 부담을 훨씬 안준다고 함
    private void batchUpdateViewCounts(Map<Long, Long> updates) {
        String sql = "UPDATE Products SET View_Count = View_Count + ? WHERE Products_IDX = ?";

        jdbcTemplate.batchUpdate(sql, updates.entrySet(), updates.size(),
                (ps, entry) -> {
                    ps.setLong(1, entry.getValue()); // 더할 값 (delta)
                    ps.setLong(2, entry.getKey());   // productId
                });
    }

}
