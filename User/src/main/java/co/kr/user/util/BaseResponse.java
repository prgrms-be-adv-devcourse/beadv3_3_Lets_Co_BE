package co.kr.user.util;

/*
 * 최종적으로 return 할 response DTO
 * 응답 예시:
 * "resultCode" : "ok"
 * "data" : { 각각의 body }
 */
public record BaseResponse<T>(
        String resultCode,
        T data
) {}
