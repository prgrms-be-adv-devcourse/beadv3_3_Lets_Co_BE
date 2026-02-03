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

    private static final String WAIT_KEY = "waitTKey";
    private static final String ACTIVE_KEY = "activeKey";

    public void registerOrder(Long userIdx) {
        String member = String.valueOf(userIdx);
        long accessTime = System.currentTimeMillis();
        redisTemplate.opsForZSet().add(WAIT_KEY, member, accessTime);
    }

    public QueueStatusInfo getStatus(Long userIdx) {
        String member = String.valueOf(userIdx);
        long now = System.currentTimeMillis();

        Double activeScore = redisTemplate.opsForZSet().score(ACTIVE_KEY, member);

        if (activeScore != null) {
            // 활동 중인 유저가 상태를 조회하면, 만료 시간을 연장(Touch) 해줌
            // 이렇게 하면 유저가 페이지에 머무는 동안은 쫓겨나지 않음
            redisTemplate.opsForZSet().add(ACTIVE_KEY, member, now);
            return new QueueStatusInfo(0L, true, "입장 완료! 주문페이지로 이동합니다.");
        }

        // 대기열 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAIT_KEY, member);
        if (rank != null) {
            return new QueueStatusInfo(rank + 1, false, "대기 중입니다.");
        }

        return new QueueStatusInfo(-1L, false, "대기열에 없습니다.");
    }

    public void allowUser(long maxCapacity) {

        Long currentActive = redisTemplate.opsForZSet().zCard(ACTIVE_KEY);
        if (currentActive == null) currentActive = 0L;

        long availableSlots = maxCapacity - currentActive;

        if (availableSlots <= 0) {
            return;
        }

        Set<Object> allowedUsers = redisTemplate.opsForZSet().range(WAIT_KEY, 0, availableSlots - 1);
        if (allowedUsers == null || allowedUsers.isEmpty()) return;

        long now = System.currentTimeMillis();
        for (Object user : allowedUsers) {
            String member = (String) user;
            redisTemplate.opsForZSet().add(ACTIVE_KEY, member, now);
            redisTemplate.opsForZSet().remove(WAIT_KEY, member);
        }
    }

    // 명시적 퇴장 (프론트에서 페이지 이탈 시 호출)
    public void exitQueue(String member) {
        redisTemplate.opsForZSet().remove(ACTIVE_KEY, member);
        redisTemplate.opsForZSet().remove(WAIT_KEY, member);
    }

    //  만료된 유저 정리
    public void evictInactiveUsers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        long cutOffTime = now - timeoutMillis; // 현재 시간 - 30분

        // Score가 cutOffTime보다 작은(오래된) 유저들을 일괄 삭제
        // removeRangeByScore: min ~ max 범위의 데이터를 삭제
        Long removedCount = redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_KEY, 0, cutOffTime);

        if (removedCount != null && removedCount > 0) {
            log.info("시간 초과로 {}명의 유저를 퇴장 처리했습니다.", removedCount);
        }
    }
}