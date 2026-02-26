package co.kr.order.controller.swagger.settlement;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "수동 정산 배치 실행",
        description = """
                전월 기준으로 정산 배치를 수동 실행한다.

                - 데모/운영 목적
                - 요청 스레드에서 동기 실행
                - 별도의 요청 데이터 x
                """
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "수동 정산 완료"),
        @ApiResponse(responseCode = "500", description = "수동 정산 실패")
})
public @interface SettlementManualDocs {
}
