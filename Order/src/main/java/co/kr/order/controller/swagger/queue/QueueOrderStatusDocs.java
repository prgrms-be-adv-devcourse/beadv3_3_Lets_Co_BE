package co.kr.order.controller.swagger.queue;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "주문 대기열 상태 조회",
        description = """
                주문 대기열 번호 및 입장 가능 여부를 조회한다.

                - 사용자 idx 필수 (헤더)
                """
)

@Parameters({
        @Parameter(
                name = "X-USERS-IDX",
                in = ParameterIn.HEADER,
                description = "사용자 idx",
                required = true,
                example = "1"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 대기열 상태 조회 성공")
})
public @interface QueueOrderStatusDocs {
}
