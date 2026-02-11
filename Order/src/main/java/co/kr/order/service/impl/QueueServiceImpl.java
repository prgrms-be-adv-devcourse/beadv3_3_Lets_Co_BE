package co.kr.order.service.impl;

import co.kr.order.model.redis.WaitingQueue;
import co.kr.order.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final RedisTemplate<String, Object> redisTemplate;

    // 입장 대기열 Key
    private static final String WAIT_ENTER_KEY = "waitEnterKey";  // 대기중
    private static final String ACTIVE_ENTER_KEY = "activeEnterKey";  // 입장 완료

    // 주문 대기열 Key
    private static final String WAIT_ORDER_KEY = "waitOrderKey";  // 대기중
    private static final String ACTIVE_ORDER_KEY = "activeOrderKey";  // 입장 완료

    // =============================
    // 1. 입장 대기열 (Rate Limiter)
    // =============================

    /*
     * 유저가 처음 진입 시 대기열(Wait Key)에 추가
     * @param queueToken 유저 식별 토큰
     */
    @Override
    public void registerEnter(String queueToken) {

        // 현재 시간을 Score로 사용
        long accessTime = System.currentTimeMillis();

        // ZSet에 저장 [Key: waitEnterKey, Value: 토큰, Score: 현재시간]
        redisTemplate.opsForZSet().add(WAIT_ENTER_KEY, queueToken, accessTime);
    }

    /*
     * 입장 대기 상태 조회 (Polling)
     * 프론트엔드에서 주기적으로 호출하여 진입 가능 여부와 남은 대기 순번을 확인
     * @param queueToken 유저 식별 토큰
     */
    @Override
    public WaitingQueue getEnterStatus(String queueToken) {

        // Active Set(입장 허용된 그룹)에 포함되어 있는지 확인
        Double activeScore = redisTemplate.opsForZSet().score(ACTIVE_ENTER_KEY, queueToken);

        if (activeScore != null) {
            // 입장 허용된 상태라면, Active Key에서 제거하여 '입장권'을 소모시킴 (일회성 통과)
            redisTemplate.opsForZSet().remove(ACTIVE_ENTER_KEY, queueToken);
            return new WaitingQueue(0L, true, "입장 완료");
        }

        // Wait Set(대기 그룹)에서의 내 순위 확인 (0부터 시작하므로 +1 처리)
        Long rank = redisTemplate.opsForZSet().rank(WAIT_ENTER_KEY, queueToken);
        if (rank != null) {
            return new WaitingQueue(rank + 1, false, "대기 중입니다.");
        }

        // 대기열에 없는 경우
        return new WaitingQueue(-1L, false, "대기열에 없습니다.");
    }

    /*
     * 1초마다 동작 (스케쥴러)
     * 현재 처리 중인 유저 수(Active)를 확인해서, 여유 공간만큼만 대기 유저를 진입
     * @param count: 주문 허용 인원
     */
    @Override
    public void allowEnterUser(Long count) {

        // 대기열에서 가장 오래된(Score가 낮은) 순서대로 count만큼 조회
        Set<Object> allowedUsers = redisTemplate.opsForZSet().range(WAIT_ENTER_KEY, 0, count - 1);

        // 없으면 return
        if (allowedUsers == null || allowedUsers.isEmpty()) return;

        // 현재 시각
        long now = System.currentTimeMillis();
        for (Object user : allowedUsers) {
            String member = (String) user;

            // Active Set으로 이동 (입장 허용)
            redisTemplate.opsForZSet().add(ACTIVE_ENTER_KEY, member, now);
            // Wait Set에서 제거
            redisTemplate.opsForZSet().remove(WAIT_ENTER_KEY, member);
        }
    }

    // =================================
    // 2. 주문 대기열 (Capacity Limiter)
    // =================================

    /*
     * 주문 대기열 등록
     * 주문 요청 시 대기열에 등록
     */
    @Override
    public void registerOrder(Long userIdx) {

        // userIdx를 Member로 사용
        String member = String.valueOf(userIdx);
        // 현재 시간을 Score로 사용
        long accessTime = System.currentTimeMillis();

        // ZSet에 저장 [Key: waitOrderKey, Value: 유저Idx, Score: 진입시간]
        redisTemplate.opsForZSet().add(WAIT_ORDER_KEY, member, accessTime);
    }

    /*
     * 주문 대기 상태 조회 및 Heartbeat (살아있는지 체크)
     * @param userIdx: 유저 인덱스
     */
    @Override
    public WaitingQueue getOrderStatus(Long userIdx) {

        // userIdx를 Member로 사용
        String member = String.valueOf(userIdx);
        // 현재 시각
        long now = System.currentTimeMillis();

        // 이미 주문 처리가 가능한 상태(Active)인지 확인
        Double activeScore = redisTemplate.opsForZSet().score(ACTIVE_ORDER_KEY, member);
        if (activeScore != null) {
            // Active 상태라면 Score를 현재 시간으로 갱신 (Heartbeat)
            // 이를 통해 작업 중인 유저가 타임아웃으로 쫓겨나지 않도록 유지
            redisTemplate.opsForZSet().add(ACTIVE_ORDER_KEY, member, now);
            return new WaitingQueue(0L, true, "주문 가능");
        }

        // 대기열에서의 순번 확인
        Long rank = redisTemplate.opsForZSet().rank(WAIT_ORDER_KEY, member);
        if (rank != null) {
            return new WaitingQueue(rank + 1, false, "대기 중입니다.");
        }

        // 대기열에 없는 경우
        return new WaitingQueue(-1L, false, "대기열에 없습니다.");
    }

    /*
     * 1초마다 동작 (스케쥴러)
     * 주문 완료 및 대기열 퇴장
     * 현재 처리 중인 유저 수(Active)를 확인하여, 여유 공간만큼만 대기 유저를 진입
     * @param maxCapacity: 최대 수용량
     */
    @Override
    public void allowOrderUser(Long maxCapacity) {
        // 현재 활동 중인(주문 프로세스 진행 중인) 유저 수 조회
        Long currentActive = redisTemplate.opsForZSet().zCard(ACTIVE_ORDER_KEY);
        if (currentActive == null) currentActive = 0L;

        // 여유 슬롯 계산 (최대 허용 - 현재 활동 중)
        long availableSlots = maxCapacity - currentActive;
        if (availableSlots <= 0) return;  // 여유가 없으면 아무도 진입시키지 않음

        // 여유 슬롯만큼 대기열(Wait) 앞에서부터 유저 조회
        Set<Object> allowedUsers = redisTemplate.opsForZSet().range(WAIT_ORDER_KEY, 0, availableSlots - 1);
        if (allowedUsers == null || allowedUsers.isEmpty()) return;


        long now = System.currentTimeMillis();
        for (Object user : allowedUsers) {
            String member = (String) user;

            // Active Set으로 이동 및 Wait Set 제거
            redisTemplate.opsForZSet().add(ACTIVE_ORDER_KEY, member, now);
            redisTemplate.opsForZSet().remove(WAIT_ORDER_KEY, member);
        }
    }

    /*
     * 대기열 이탈 (주문 완료/취소 시)
     * 로직이 끝난 유저를 대기열 시스템에서 완전히 제거하여 슬롯을 확보
     */
    @Override
    public void exitQueue(Long userIdx) {
        String member = String.valueOf(userIdx);

        // Active Set으로 이동 및 Wait Set 제거
        redisTemplate.opsForZSet().remove(ACTIVE_ORDER_KEY, member);
        redisTemplate.opsForZSet().remove(WAIT_ORDER_KEY, member);
    }

    /*
     * 1초마다 동작 (스케쥴러)
     * 비활성 유저 자동 퇴장 (Timeout 처리)
     * 일정 시간 동안 활동(Heartbeat)이 없는 유저를 Active 목록에서 제거
     * 중간에 이탈한 유저 때문에 슬롯이 낭비되는 것을 방지
     * @param timeoutMillis: 만료 시간
     */
    @Override
    public void evictInactiveUsers(long timeoutMillis) {
        long now = System.currentTimeMillis();
        long cutOffTime = now - timeoutMillis;  // 이 시간 이전에 활동한 유저는 퇴장 대상

        // Score(마지막 활동 시간)가 cutOffTime보다 작은(오래된) 멤버들 삭제
        Long removedCount = redisTemplate.opsForZSet().removeRangeByScore(ACTIVE_ORDER_KEY, 0, cutOffTime);

        if (removedCount != null && removedCount > 0) {
            log.info("주문 대기열: 시간 초과로 {}명의 유저를 자동 퇴장 처리했습니다.", removedCount);
        }
    }
}
