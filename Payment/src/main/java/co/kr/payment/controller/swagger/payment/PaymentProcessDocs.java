package co.kr.payment.controller.swagger.payment;

import co.kr.payment.model.dto.request.PaymentReq;
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
        summary = "결제 처리",
        description = """
                주문에 대한 결제를 처리한다.

                - 사용자 idx 필수 (헤더)
                - 결제 수단: DEPOSIT(예치금), TOSS_PAY, CARD
                - TOSS_PAY인 경우 tossKey 필수
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
                schema = @Schema(implementation = PaymentReq.class),
                examples = {
                        @ExampleObject(
                                value = """
                                    {
                                      "orderCode": "ORD-20260226-001",
                                      "userInfo": {
                                        "addressInfo": {
                                          "recipient": "홍길동",
                                          "address": "서울특별시 강남구 테헤란로 123",
                                          "addressDetail": "4층 401호",
                                          "phone": "010-1234-5678"
                                        },
                                        "cardInfo": {
                                          "cardBrand": "VISA",
                                          "cardName": "홍길동",
                                          "cardToken": "card_token_xyz789",
                                          "expMonth": 12,
                                          "expYear": 2028
                                        }
                                      },
                                      "paymentType": "CARD",
                                      "amount": 35000,
                                      "tossKey": null
                                    }
                                    """
                        )
                }
        )
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "결제 처리 성공"),
        @ApiResponse(responseCode = "400", description = """
                - ALREADY_PAID: 이미 결제된 주문
                - PAYMENT_KEY_NOT_FOUND: 토스페이 PaymentKey 누락
                - PAYMENT_FAILED: 결제 승인 실패 (예치금 차감 실패, 토스 승인 실패)
                - INVALID_INPUT_VALUE: 유효성 검증 실패""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "ALREADY_PAID",
                                      "data": "이미 결제된 주문입니다."
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
public @interface PaymentProcessDocs {
}
