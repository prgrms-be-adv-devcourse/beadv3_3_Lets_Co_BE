package co.kr.order.model.dto;

public record GetOrderData(
        Long usersIdx,
        Long addressIdx,
        Long cardIdx
) {}