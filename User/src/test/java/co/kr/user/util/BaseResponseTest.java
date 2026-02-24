package co.kr.user.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BaseResponse(java/co/kr/user/util/BaseResponse.java) 단위 테스트
 */
@DisplayName("BaseResponse 단위 테스트")
class BaseResponseTest {

    @Test
    @DisplayName("성공 응답 생성 테스트: 결과 코드와 데이터가 정확히 매칭되어야 함")
    void successResponseTest() {
        // Given
        String data = "Success Content";
        String resultCode = "SUCCESS";

        // When - Record의 생성자 규격(String, T)에 맞게 인자 전달
        BaseResponse<String> response = new BaseResponse<>(resultCode, data);

        // Then - getStatus(), getMessage() 대신 record의 필드 접근 메서드 사용
        assertEquals(resultCode, response.resultCode());
        assertEquals(data, response.data());
    }

    @Test
    @DisplayName("에러 응답 생성 테스트: 에러 코드 설정 시 데이터는 null일 수 있음")
    void errorResponseTest() {
        // Given
        String errorCode = "ERROR_001";

        // When - int 상태값이 아닌 String 결과 코드를 전달
        BaseResponse<Object> response = new BaseResponse<>(errorCode, null);

        // Then
        assertEquals(errorCode, response.resultCode());
        assertNull(response.data());
    }
}