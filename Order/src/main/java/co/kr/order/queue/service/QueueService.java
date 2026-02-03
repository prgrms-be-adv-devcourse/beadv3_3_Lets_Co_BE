package co.kr.order.queue.service;

import co.kr.order.queue.model.dto.QueueStatusInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    private static final String WAIT_ENTER_KEY = "waitEnterKey";
    private static final String ACTIVE_ENTER_KEY = "activeEnterKey";

    private static final String WAIT_ORDER_KEY = "waitOrderKey";
    private static final String ACTIVE_ORDER_KEY = "activeOrderKey";

    // ==========================
    // 1. 입장 대기열 (Rate Limiter)
    // ==========================
    public void registerEnter(String queueToken) {
        long accessTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAIT_ENTER_KEY, queueToken, accessTime);
    }

    public QueueStatusInfo getEnterStatus(String queueToken) {
        Double activeScore = redisTemplate.opsForZSet().score(ACTIVE_ENTER_KEY, queueToken);

        if (activeScore != null) {
            redisTemplate.opsForZSet().remove(ACTIVE_ENTER_KEY, queueToken);

            return new QueueStatusInfo(0L, true, "입장 완료");
        }

        Long rank = redisTemplate.opsForZSet().rank(WAIT_ENTER_KEY, queueToken);
        if (rank != null) {
            return new QueueStatusInfo(rank + 1, false, "대기 중입니다.");
        }

        return new QueueStatusInfo(-1L, false, "대기열에 없습니다.");
    }

    public void allowEnterUser(long count) {
        Set<Object> allowedUsers = redisTemplate.opsForZSet().range(WAIT_ENTER_KEY, 0, count - 1);
        if (allowedUsers == null || allowedUsers.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (Object user : allowedUsers) {
            String member = (String) user;
            redisTemplate.opsForZSet().add(ACTIVE_ENTER_KEY, member, now);
            redisTemplate.opsForZSet().remove(WAIT_ENTER_KEY, member);
        }
    }

    // ==========================
    // 2. 주문 대기열 (Capacity Limiter)
    // ==========================
    public void registerOrder(Long userIdx) {
        String member = String.valueOf(userIdx);
        long accessTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAIT_ORDER_KEY, member, accessTime);
    }

    public QueueStatusInfo getOrderStatus(Long userIdx) {
        String member = String.valueOf(userIdx);
        long now = System.currentTimeMillis();

        Double activeScore = redisTemplate.opsForZSet().score(ACTIVE_ORDER_KEY, member);
        if (activeScore != null) {
            redisTemplate.opsForZSet().add(ACTIVE_ORDER_KEY, member, now);
            return new QueueStatusInfo(0L, true, "주문 가능");
        }

        Long rank = redisTemplate.opsForZSet().rank(WAIT_ORDER_KEY, member);
        if (rank != null) {
            return new QueueStatusInfo(rank + 1, false, "대기 중입니다.");
        }

        return new QueueStatusInfo(-1L, false, "대기열에 없습니다.");
    }

    public void allowOrderUser(long maxCapacity) {
        Long currentActive = redisTemplate.opsForZSet().zCard(ACTIVE_ORDER_KEY);
        if (currentActive == null) currentActive = 0L;

        long availableSlots = maxCapacity - currentActive;
        if (availableSlots <= 0) return;

        Set<Object> allowedUsers = redisTemplate.opsForZSet().range(WAIT_ORDER_KEY, 0, availableSlots - 1);
        if (allowedUsers == null || allowedUsers.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (Object user : allowedUsers) {
            String member = (String) user;
            redisTemplate.opsForZSet().add(ACTIVE_ORDER_KEY, member, now);
            redisTemplate.opsForZSet().remove(WAIT_ORDER_KEY, member);
        }
    }

    public void exitQueue(Long userIdx) {
        String member = String.valueOf(userIdx);
        redisTemplate.opsForZSet().remove(ACTIVE_ORDER_KEY, member);
        redisTemplate.opsForZSet().remove(WAIT_ORDER_KEY, member);
    }

    public void evictInactiveUsers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        long cutOffTime = now - timeoutMillis;

        Long removedCount = redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_ORDER_KEY, 0, cutOffTime);

        if (removedCount != null && removedCount > 0) {
            log.info("주문 대기열: 시간 초과로 {}명의 유저를 자동 퇴장 처리했습니다.", removedCount);
        }
    }
}