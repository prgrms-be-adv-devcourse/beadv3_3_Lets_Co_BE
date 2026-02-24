package co.kr.product.product.controller.swagger.category;


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
        summary = "특정 카테고리의 부모/자식 조회",
        description = """
                카테고리 부모/자식 조회
                
                - 사용자 idx 필요 x
                 > 회원/비회원 관계 없이 접근 가능
                
                """
)

@Parameters({
        @Parameter(
                name = "categoryCode",
                in = ParameterIn.PATH,
                description = "카테고리 Code",
                required = true,
                example = "fddca2e1-340f-47bd-8dd0-0096b34aa24e"
        )
})


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 부모/자식 조회 성공")
})
public @interface CategoryFamilyListDocs {
}
