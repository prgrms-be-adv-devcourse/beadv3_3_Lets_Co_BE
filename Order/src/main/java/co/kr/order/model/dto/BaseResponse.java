package co.kr.order.model.dto;

public record BaseResponse<T>(
        String resultCode,
        T data
) {}
