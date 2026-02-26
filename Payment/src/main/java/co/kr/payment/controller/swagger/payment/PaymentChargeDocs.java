package co.kr.payment.controller.swagger.payment;

import co.kr.payment.model.dto.request.ChargeReq;
import co.kr.payment.model.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "예치금 충전",
        description = """
                예치금을 충전한다.

                - 사용자 idx 필수 (헤더)
                - 충전 금액 및 결제 수단 필수
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

@RequestBody(
        content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ChargeReq.class),
                examples = {
                        @ExampleObject(
                                value = """
                                    {
                                      "amount": 50000,
                                      "paymentType": "TOSS_PAY"
                                    }
                                    """
                        )
                }
        )
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "예치금 충전 성공"),
        @ApiResponse(responseCode = "400", description = """
                - INVALID_INPUT_VALUE: 잘못된 입력 값 (예치금으로 충전 불가)
                - CHARGE_FAILED: 예치금 충전 실패""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "CHARGE_FAILED",
                                      "data": "예치금 충전에 실패했습니다."
                                    }
                                    """)
                )),
        @ApiResponse(responseCode = "500", description = "서버 내부 오류 (FeignException 등)",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "INTERNAL_SERVER_ERROR",
                                      "data": "내부 서비스 통신 중 오류가 발생했습니다."
                                    }
                                    """)
                ))
})
public @interface PaymentChargeDocs {
}
