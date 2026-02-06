package co.kr.order.service;

import co.kr.order.model.dto.QueueStatusInfo;

public interface QueueService {
    // 입장 대기열
    QueueStatusInfo getEnterStatus(String queueToken);
    void registerEnter(String queueToken);
    void allowEnterUser(Long capacity);

    // 주문 대기열
    QueueStatusInfo getOrderStatus(Long userIdx);
    void registerOrder(Long userIdx);
    void allowOrderUser(Long capacity);
    void exitQueue(Long userIdx);
    void evictInactiveUsers(long time);
}
