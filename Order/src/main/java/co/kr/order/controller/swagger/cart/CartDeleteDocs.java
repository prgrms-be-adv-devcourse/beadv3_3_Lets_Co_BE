package co.kr.order.controller.swagger.cart;

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
        summary = "장바구니 상품 삭제",
        description = """
                장바구니에서 상품을 삭제한다.

                - 사용자 idx 필수 (헤더)
                - 옵션 코드로 삭제 대상 지정
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
        @ApiResponse(responseCode = "204", description = "장바구니 상품 삭제 성공")
})
public @interface CartDeleteDocs {
}
