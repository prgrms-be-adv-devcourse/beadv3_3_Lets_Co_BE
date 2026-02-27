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
        summary = "판매자 상품 조회",
        description = """
                판매자 본인의 상품 목록 리스트 조회
                
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
                name = "page",
                in = ParameterIn.QUERY,
                description = "페이지 번호 (0부터 시작)",
                example = "0"
        ),
        @Parameter(
                name = "size",
                in = ParameterIn.QUERY,
                description = "페이지 크기",
                example = "15"
        ),
        @Parameter(
                name = "search",
                in = ParameterIn.QUERY,
                description = "검색어",
                example = "테스트"
        ),
        @Parameter(
                name = "category",
                in = ParameterIn.QUERY,
                description = "카테고리 이름",
                example = "피규어"
        ),
        @Parameter(
                name = "ip",
                in = ParameterIn.QUERY,
                description = "ip 이름",
                example = "만화"
        )

})


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "상품 조회 성공")
})
public @interface SellerProductListDocs {
}
