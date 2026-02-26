package co.kr.assistant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@Slf4j
@EnableFeignClients // 추가됨: Feign Client 활성화
@SpringBootApplication
@RequiredArgsConstructor
public class AssistantApplication {

    private final RedisTemplate<String, Object> redisTemplate;

    public static void main(String[] args) {
        SpringApplication.run(AssistantApplication.class, args);
    }

    /**
     * [추가됨 - 개선 4번] 서버 시작 시 Redis 내의 잔여 락(Lock) 데이터를 청소합니다.
     * 서버 비정상 종료 시 남아있을 수 있는 락을 제거하여 사용자의 접속 차단을 방지합니다.
     */
    @Bean
    public CommandLineRunner clearChatLocks() {
        return args -> {
            try {
                // "lock:chat:"으로 시작하는 모든 키를 조회 (분산 락 키 패턴)
                Set<String> keys = redisTemplate.keys("lock:chat:*");

                if (keys != null && !keys.isEmpty()) {
                    log.info("서버 시작: 잔여 락(Lock) 데이터 {}개를 발견하여 청소합니다.", keys.size());
                    redisTemplate.delete(keys);
                } else {
                    log.info("서버 시작: 정리할 잔여 락(Lock) 데이터가 없습니다.");
                }
            } catch (Exception e) {
                log.error("서버 시작 중 Redis 락 데이터 청소 실패: {}", e.getMessage());
            }
        };
    }
}