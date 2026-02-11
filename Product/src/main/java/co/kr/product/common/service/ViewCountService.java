package co.kr.product.common.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ViewCountService {

    @Value("${custom.redis.product-view-count-key}")
    private String viewCountKey;

    @Value("${custom.redis.product-view-dirty-key}")
    private String dirtyKey;

    private final RedisTemplate<String, Object> redisTemplate;


    // Redis를 통한 조회수 증가
    public void increaseViewCountProduct(Long productIdx){

        redisTemplate.opsForValue().increment(viewCountKey);

        // 숫자 그대로 저장하면 나중에 뺄 때 타입 오류 생김.
        // Jackson이 큰 수냐 작은 수냐에 따라 Integer을 뱉기도 하고 Long을 뱉기도 함.
        redisTemplate.opsForSet().add(dirtyKey, String.valueOf(productIdx));

    }
}
