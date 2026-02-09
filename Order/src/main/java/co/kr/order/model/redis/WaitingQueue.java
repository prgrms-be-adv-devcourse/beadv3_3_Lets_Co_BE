package co.kr.order.model.redis;

public record WaitingQueue(
    Long rank,
    boolean isAllowed,
    String message
) {}
