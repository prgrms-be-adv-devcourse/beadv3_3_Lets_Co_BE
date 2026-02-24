package co.kr.product.product.controller.swagger.category;


import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "카테고리 등록",
        description = """
                관리자의 카테고리 등록
                
                - 관리자 권한 확인 용 사용자 idx 필수
                
                """
)

@Parameters({
        @Parameter(
                name = "X-USERS-IDX",
                in = ParameterIn.HEADER,
                description = "사용자 idx",
                required = true,
                example = "1"
        )
})


@RequestBody(
        content = @Content(
                mediaType = "application/json",

                schema = @Schema(implementation = CategoryUpsertReq.class),
                // 예시 요청 데이터 설정
                examples = {
                        @ExampleObject(
                                name = "카테고리 추가 요청",
                                value = """
                                    {
                                      "categoryName": "레진 피규어",
                                      "parentCode": "CAT-001",

                                    """
                        )
                }


        )
)


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 추가 성공")
})
public @interface CategoryAddDocs {
}
