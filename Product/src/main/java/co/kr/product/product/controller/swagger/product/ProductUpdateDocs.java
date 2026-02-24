package co.kr.product.product.controller.swagger.product;

import co.kr.product.product.model.dto.request.UpsertProductReq;
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
        summary = "상품 수정",
        description = """
                판매자,관리자의 상품 수정
                
                - 판매자 or 관리자 권한 확인 용 사용자 idx 필수
                
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


@RequestBody(
        content = @Content(
                mediaType = "application/json",

                schema = @Schema(implementation = UpsertProductReq.class),
                // 예시 요청 데이터 설정
                examples = {
                        @ExampleObject(
                                name = "상품 수정 요청",
                                value = """
                                    {
                                      "name": "테스트 상품2",
                                      "status": "ON_SALE",
                                      "categoryCode": "CAT-001",
                                      "ipCode": "2006967e-81c3-4756-9330-fc2ffe7067c2",
                                      "stock": 100,
                                      "description": "상품 수정 테스트!!",
                                      "salePrice": 10000,
                                      "price": 11000,
                                      "options": [
                                        {
                                          "code": "c06cda42-c907-4342-9bcd-41d7a7c1d7cd"
                                          "name": "테스트 상품 옵션 1",
                                          "sortOrder": 1,
                                          "price": 11000,
                                          "salePrice": 10000,
                                          "stock": 100,
                                          "status": "ON_SALE"
                                        },
                    
                                      ]
                                    }
                                    """
                        )
                }


        )
)


// 예시 응답들 설정 
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "상품 수정 성공")
})
public @interface ProductUpdateDocs {
}
