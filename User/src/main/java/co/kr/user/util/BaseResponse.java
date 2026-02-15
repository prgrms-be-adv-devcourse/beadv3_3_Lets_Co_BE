package co.kr.user.util;

/**
 * API 응답을 통일된 형식으로 반환하기 위한 래퍼(Wrapper) 클래스입니다.
 * Java 14 이상에서 도입된 Record 타입을 사용하여 불변(Immutable) 데이터 객체를 간결하게 정의합니다.
 * * @param <T> 응답 데이터의 타입 (Generic)
 */
public record BaseResponse<T>(
        /**
         * 응답 결과 코드 (예: "SUCCESS", "FAIL", "ERROR" 등)
         * 클라이언트가 요청 처리 결과를 식별하는 데 사용됩니다.
         */
        String resultCode,

        /**
         * 실제 응답 데이터 (Payload)
         * 요청에 따른 결과 객체가 담깁니다. (성공 시 DTO, 실패 시 에러 메시지 등)
         */
        T data
) {}