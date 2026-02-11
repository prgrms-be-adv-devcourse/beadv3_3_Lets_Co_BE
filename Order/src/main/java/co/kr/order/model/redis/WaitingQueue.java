package co.kr.order.model.redis;

/*
 * 입장 대기열 번호 응답
 * @param rank: 남은 대기열 번호
 * @param isAllowed: 입장 가능 여부
 * @param message: 화면 출력 메시지
 */
public record WaitingQueue(
    Long rank,
    boolean isAllowed,
    String message
) {}
