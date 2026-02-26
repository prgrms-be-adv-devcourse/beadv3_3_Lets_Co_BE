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
        summary = "판매자 정산 내역 목록 조회",
        description = """
                특정 판매자의 정산 내역 목록을 조회한다.

                - 판매자 idx 필수
                - 정산 유형(ORDERS_CONFIRMED, SETTLE_PAYOUT, CANCEL_ADJUST) 포함
                """
)

@Parameters({
        @Parameter(
                name = "sellerIdx",
                in = ParameterIn.PATH,
                description = "판매자 idx",
                required = true,
                example = "1"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "정산 내역 목록 조회 성공")
})
public @interface SettlementListDocs {
}
