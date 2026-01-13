package co.kr.order.model.dto;

/**
 * @param usersIdx
 * @param addressIdx
 * @param cardIdx
 * 주문 할 때 필요한 id들
 */
public record UserData(
        Long usersIdx,
        Long addressIdx,
        Long cardIdx
) {}