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
        summary = "상품 검색",
        description = """
                전체 상품 목록 조회 및 검색.
                
                - 사용자 idx 필요 x
                 > 회원/비회원 상관 없이 접근 가능
                """
)

@Parameters({
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
                example = "20"
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
public @interface ProductListDocs {
}
