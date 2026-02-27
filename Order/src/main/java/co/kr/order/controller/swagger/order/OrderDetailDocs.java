package co.kr.order.controller.swagger.order;

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
        summary = "주문 상세 조회",
        description = """
                주문 코드로 주문 상세 정보를 조회한다.

                - 사용자 idx 필수 (헤더)
                - 본인의 주문만 조회 가능
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
                name = "orderCode",
                in = ParameterIn.PATH,
                description = "주문 코드",
                required = true,
                example = "c06cda42-c907-4342-9bcd-41d7a7c1d7cd"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 상세 조회 성공"),
        @ApiResponse(responseCode = "404", description = """
                - ORDER_NOT_FOUND: 주문 정보를 찾을 수 없음""",
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = BaseResponse.class),
                        examples = @ExampleObject(value = """
                                    {
                                      "resultCode": "ORDER_NOT_FOUND",
                                      "data": "주문 정보를 찾을 수 없습니다"
                                    }
                                    """)
                ))
})
public @interface OrderDetailDocs {
}
