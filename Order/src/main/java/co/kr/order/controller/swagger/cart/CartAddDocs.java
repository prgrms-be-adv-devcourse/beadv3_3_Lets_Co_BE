package co.kr.order.controller.swagger.cart;

import co.kr.order.model.dto.ProductInfo;
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
        summary = "장바구니 상품 추가",
        description = """
                장바구니에 상품을 추가한다.

                - 사용자 idx 필수 (헤더)
                - 상품 코드, 옵션 코드, 수량 필수
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
                schema = @Schema(implementation = ProductInfo.class),
                examples = {
                        @ExampleObject(
                                value = """
                                    {
                                      "productCode": "c06cda42-c907-4342-9bcd-41d7a7c1d7cd",
                                      "optionCode": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
                                      "quantity": 1
                                    }
                                    """
                        )
                }
        )
)

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "201", description = "장바구니 추가 성공"),
        @ApiResponse(responseCode = "400", description = """
                - BAD_REQUEST_VALID: 유효성 검증 실패""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "BAD_REQUEST_VALID",
                                      "data": "상품 코드는 필수입니다."
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
public @interface CartAddDocs {
}
