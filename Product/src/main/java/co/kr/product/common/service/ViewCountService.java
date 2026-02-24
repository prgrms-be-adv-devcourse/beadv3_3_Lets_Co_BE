package co.kr.product.common.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ViewCountService {

    @Value("${custom.redis.product-view-count-key}")
    private String viewCountKey;

    @Value("${custom.redis.product-view-dirty-key}")
    private String dirtyKey;

    private final StringRedisTemplate stringRedisTemplate;


    // Redis를 통한 조회수 증가
    public Long increaseViewCountProduct(Long productIdx){

        // 조회수 증가
        Long viewCount = stringRedisTemplate.opsForValue().increment(viewCountKey + productIdx);

        // 숫자 그대로 저장하면 나중에 뺄 때 타입 오류 생김.
        // Jackson이 큰 수냐 작은 수냐에 따라 Integer을 뱉기도 하고 Long을 뱉기도 함.
        stringRedisTemplate.opsForSet().add(dirtyKey, String.valueOf(productIdx));

        return viewCount;
    }
}
