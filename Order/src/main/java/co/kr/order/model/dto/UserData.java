package co.kr.order.model.dto;

/*
 * @param usersIdx
 * @param addressIdx
 * @param cardIdx
 */
public record UserData(
        Long usersIdx,
        Long addressIdx,
        Long cardIdx
) {}