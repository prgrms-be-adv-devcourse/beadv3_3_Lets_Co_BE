package co.kr.order.controller.swagger.cart;

import co.kr.order.model.dto.response.BaseResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "장바구니 수량 +1",
        description = """
                장바구니 상품의 수량을 1 증가시킨다.

                - 사용자 idx 필수 (헤더)
                - 옵션 코드로 대상 상품 지정
                """
)

@Parameters({
        @Parameter(
                name = "X-USERS-IDX",
                in = ParameterIn.HEADER,
                description = "사용자 idx",
                required = true,
                example = "1"
        ),
        @Parameter(
                name = "optionCode",
                in = ParameterIn.PATH,
                description = "상품 옵션 코드",
                required = true,
                example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "수량 증가 성공"),
        @ApiResponse(responseCode = "404", description = """
                - CART_NOT_FOUND: 장바구니 정보를 찾을 수 없음""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "CART_NOT_FOUND",
                                      "data": "장바구니 정보를 찾을 수 없습니다."
                                    }
                                    """)
                ))
})
public @interface CartPlusDocs {
}
