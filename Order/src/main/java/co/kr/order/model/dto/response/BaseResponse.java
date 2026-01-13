package co.kr.order.model.dto.response;

public record BaseResponse<T>(
        String resultCode,
        T data
) {}
