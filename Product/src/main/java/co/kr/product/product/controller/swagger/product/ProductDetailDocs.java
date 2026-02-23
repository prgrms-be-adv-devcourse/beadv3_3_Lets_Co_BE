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
        summary = "상품 상세 조회",
        description = """
                상품 상세 정보 조회
                
                - 사용자 idx 필요 x
                 > 회원/비회원 관계 없이 접근 가능
                
                """
)

@Parameters({
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
        @ApiResponse(responseCode = "200", description = "상품 조회 성공")
})
public @interface ProductDetailDocs {
}
