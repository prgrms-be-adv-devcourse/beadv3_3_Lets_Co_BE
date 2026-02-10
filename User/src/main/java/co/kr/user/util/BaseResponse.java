package co.kr.user.util;

public record BaseResponse<T>(
        String resultCode,
        T data
) {}
