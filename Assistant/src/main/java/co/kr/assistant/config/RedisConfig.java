package co.kr.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * Redis 설정 클래스입니다.
 * Redis 연결 및 데이터 직렬화 방식을 설정합니다.
 */
@Configuration
public class RedisConfig {

    /**
     * Redis 작업을 위한 RedisTemplate 빈을 등록합니다.
     * 키와 값의 직렬화 방식을 설정하여 데이터가 올바르게 저장되고 조회되도록 합니다.
     * @param connectionFactory Redis 연결 팩토리 (Spring Boot가 자동 구성한 것을 주입받음)
     * @return 설정된 RedisTemplate 객체
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();

        // 연결 팩토리 설정
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key 직렬화 설정: StringRedisSerializer 사용 (일반적인 문자열 키)
        redisTemplate.setKeySerializer(new StringRedisSerializer());

        // ValueSerializer로 StringRedisSerializer를 사용하고 있습니다.
        // 따라서 Redis에 객체를 저장할 때 자동으로 JSON 직렬화가 되지 않으므로,
        // Service단에서 직접 ObjectMapper로 JSON 문자열로 변환 후 저장해야 합니다.
        // (현재 코드에서는 LoginServiceImpl 등에서 String만 저장하므로 문제는 없습니다.)
        redisTemplate.setValueSerializer(new StringRedisSerializer());

        return redisTemplate;
    }
}