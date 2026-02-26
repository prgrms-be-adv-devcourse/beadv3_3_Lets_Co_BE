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
        summary = "입장 대기열 상태 조회",
        description = """
                입장 대기열 번호 및 입장 가능 여부를 조회한다.

                - 대기열 등록 시 발급받은 토큰 필수 (헤더)
                """
)

@Parameters({
        @Parameter(
                name = "X-QUEUE-TOKEN",
                in = ParameterIn.HEADER,
                description = "대기열 토큰",
                required = true,
                example = "1"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "대기열 상태 조회 성공")
})
public @interface QueueEnterStatusDocs {
}
