package co.kr.product.product.controller.swagger.product;

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
        summary = "관리자, 판매자 용 상품 상세 정보",
        description = """
                관리자, 판매자의 상품 상세 조회
                
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
                name = "code",
                in = ParameterIn.PATH,
                description = "상품 Code",
                required = true,
                example = "c06cda42-c907-4342-9bcd-41d7a7c1d7cd"
        )
})


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "상품 상세 조회 성공")
})
public @interface ManagerProductDetailDocs {
}
