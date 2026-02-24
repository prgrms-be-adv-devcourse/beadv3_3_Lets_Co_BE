package co.kr.product.product.controller.swagger.ip;


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
        summary = "IP 등록",
        description = """
                관리자의 IP 등록
                
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
                                name = "IP 추가 요청",
                                value = """
                                    {
                                      "categoryName": "귀멸의 칼날",
                                      "parentCode": "2006967e-81c3-4756-9330-fc2ffe7067c2",

                                    """
                        )
                }


        )
)


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "IP 추가 성공")
})
public @interface IpAddDocs {
}
