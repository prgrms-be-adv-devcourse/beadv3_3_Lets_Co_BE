package co.kr.product.common;

import org.springframework.http.HttpStatusCode;

/*
 * 최종적으로 return 할 response DTO
 * 응답 예시:
 * "resultCode" : "ok"
 * "data" : { 각각의 body }
 */
public record BaseResponse<T>(
        String resultCode,
        T data
) {
    public static <T> BaseResponse<T> ok (T data){
        return new BaseResponse<>("ok",data);
    }

}

