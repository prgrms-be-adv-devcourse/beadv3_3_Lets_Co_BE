package co.kr.order.domain.model.dto;

public record BaseResponse<T>(
        String resultCode,
        T data
) {}
