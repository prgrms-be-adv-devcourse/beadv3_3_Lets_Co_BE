package co.kr.order.model.dto.response;

/*
 * 최종적으로 Front에 보낼 응답 형식
 * 응답 예시:
 * "resultCode" : "ok"
 * "data" : { 어느 타입이든 올 수 있음 (Object/String/Integer ...) }
 */
public record BaseResponse<T>(
        String resultCode,
        T data
) {}
