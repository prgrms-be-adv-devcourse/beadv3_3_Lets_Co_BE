package co.kr.order.controller.swagger.order;

import co.kr.order.model.dto.request.OrderReq;
import co.kr.order.model.dto.response.BaseResponse;
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
        summary = "주문 요청",
        description = """
                주문을 생성한다.

                - 사용자 idx 필수 (헤더)
                - 주문 타입: DIRECT(직접 주문), CART(장바구니 주문)
                - DIRECT인 경우 productInfo 필수
                - CART인 경우 장바구니에 담긴 상품으로 자동 주문
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
                schema = @Schema(implementation = OrderReq.class),
                examples = {
                        @ExampleObject(
                                value = """
                                    {
                                      "orderType": "DIRECT",
                                      "productInfo": {
                                        "productCode": "c06cda42-c907-4342-9bcd-41d7a7c1d7cd",
                                        "optionCode": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                        "quantity": 2
                                      }
                                    }
                                    """
                        )
                }
        )
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "주문 생성 성공"),
        @ApiResponse(responseCode = "400", description = """
                - OUT_OF_STOCK: 재고가 부족합니다
                - ORDER_REFUND_EXCEPTION: 주문 후처리 중 오류가 발생하여 자동 환불
                - BAD_REQUEST_VALID: 유효성 검증 실패""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "OUT_OF_STOCK",
                                      "data": "재고가 부족합니다."
                                    }
                                    """)
                )),
        @ApiResponse(responseCode = "404", description = """
                - PRODUCT_NOT_FOUND: 제품을 찾을 수 없음""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "PRODUCT_NOT_FOUND",
                                      "data": "제품을 찾을 수 없습니다."
                                    }
                                    """)
                ))
})
public @interface OrderCreateDocs {
}
