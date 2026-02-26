package co.kr.order.controller.swagger.settlement;

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
        summary = "정산 상세 조회",
        description = """
                특정 판매자의 결제 건에 대한 정산 상세 정보를 조회한다.

                - 판매자 idx, 결제 idx 필수
                """
)

@Parameters({
        @Parameter(
                name = "sellerIdx",
                in = ParameterIn.PATH,
                description = "판매자 idx",
                required = true,
                example = "1"
        ),
        @Parameter(
                name = "paymentIdx",
                in = ParameterIn.PATH,
                description = "결제 idx",
                required = true,
                example = "1"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "정산 상세 조회 성공")
})
public @interface SettlementDetailDocs {
}
