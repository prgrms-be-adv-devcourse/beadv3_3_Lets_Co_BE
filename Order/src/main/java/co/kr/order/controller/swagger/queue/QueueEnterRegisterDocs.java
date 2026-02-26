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
        summary = "입장 대기열 등록",
        description = """
                입장 대기열에 등록한다. (Rate Limiter)

                - 회원: X-USERS-IDX를 토큰으로 사용
                - 비회원: 랜덤 UUID 토큰 발급
                - 반환된 토큰으로 대기열 상태 조회 가능
                """
)

@Parameters({
        @Parameter(
                name = "X-USERS-IDX",
                in = ParameterIn.HEADER,
                description = "사용자 idx (비회원은 생략 가능)",
                required = false,
                example = "1"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "대기열 등록 성공 (토큰 반환)")
})
public @interface QueueEnterRegisterDocs {
}
