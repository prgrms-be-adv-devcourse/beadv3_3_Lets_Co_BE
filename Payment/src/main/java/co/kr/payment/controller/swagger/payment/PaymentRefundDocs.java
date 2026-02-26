package co.kr.payment.controller.swagger.payment;

import co.kr.payment.model.dto.request.RefundReq;
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
        summary = "환불 처리",
        description = """
                결제 건에 대한 환불을 처리한다.

                - 사용자 idx 필수 (헤더)
                - 주문 코드(orderCode) 기반으로 환불 대상 결정
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
                schema = @Schema(implementation = RefundReq.class),
                examples = {
                        @ExampleObject(
                                value = """
                                    {
                                      "orderCode": "ORD-20260226-001"
                                    }
                                    """
                        )
                }
        )
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "환불 처리 성공"),
        @ApiResponse(responseCode = "400", description = """
                - PAYMENT_NOT_FOUND: 결제 내역을 찾을 수 없음
                - ALREADY_CANCELLED_PAYMENT: 이미 취소된 결제 건
                - USER_MISMATCH: 유효한 유저가 아님
                - PAYMENT_KEY_NOT_FOUND: 토스페이 PaymentKey 누락
                - PAYMENT_CANCEL_FAILED: 결제 취소 요청 실패
                - INVALID_INPUT_VALUE: 유효성 검증 실패""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "PAYMENT_NOT_FOUND",
                                      "data": "결제 내역을 찾을 수 없습니다."
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
public @interface PaymentRefundDocs {
}
