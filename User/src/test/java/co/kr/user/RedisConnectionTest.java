package co.kr.user;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

@SpringBootTest
class RedisConnectionTest {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Test
    void redis_연결_테스트() {
        // Given
        ValueOperations<String, String> valueOperations = redisTemplate.opsForValue();
        String key = "testKey";
        String value = "hello redis";

        // When
        valueOperations.set(key, value);

        // Then
        String result = valueOperations.get(key);
        Assertions.assertThat(result).isEqualTo(value);
    }
}