package co.kr.product.product.controller.swagger.category;


import co.kr.product.product.model.dto.request.CategoryUpsertReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "카테고리 수정",
        description = """
                관리자의 카테고리 수정
                
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
        ),
        @Parameter(
                name = "categoryCode",
                in = ParameterIn.PATH,
                description = "수정 할 카테고리의 Code",
                required = true,
                example = "fddca2e1-340f-47bd-8dd0-0096b34aa24e"

        )
})


@RequestBody(
        content = @Content(
                mediaType = "application/json",

                schema = @Schema(implementation = CategoryUpsertReq.class),
                // 예시 요청 데이터 설정
                examples = {
                        @ExampleObject(
                                name = "카테고리 수정 요청",
                                value = """
                                    {
                                      "categoryName": "후드티",
                                      "parentCode": "e9daf774-80d1-48bc-afb6-8dc9a996b0c0",

                                    """
                        )
                }


        )
)


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
})
public @interface CategoryUpdateDocs {
}
