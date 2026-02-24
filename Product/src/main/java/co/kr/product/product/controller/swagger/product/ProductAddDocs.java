package co.kr.product.product.controller.swagger.product;

import co.kr.product.product.model.dto.request.UpsertProductReq;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.*;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.MediaType;

import java.lang.annotation.*;



@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "상품 추가",
        description = """
                판매자의 새로운 상품 추가
                
                - 판매자 권한 확인 용 사용자 idx 필수
                - 이미지 최소 한 개 필수
                - 옵션 최소 한 개 필수
                - 카테고리, ip 지정 필수
                
                """
)

@Parameter(
        name = "X-USERS-IDX",
        in = ParameterIn.HEADER,
        description = "사용자 idx",
        required = true,
        example = "1"
)

@RequestBody(
        content = @Content(
                mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                encoding = @Encoding(name = "request", contentType = MediaType.APPLICATION_JSON_VALUE),
                schemaProperties = {
                        //  JSON 데이터 (request)
                        @SchemaProperty(
                                name = "request",
                                schema = @Schema(
                                        implementation = UpsertProductReq.class,
                                        description = "상품 정보 JSON",
                                        example = """
                                                {
                                                  "name": "테스트 상품",
                                                  "status": "ON_SALE",
                                                  "categoryCode": "CAT-001",
                                                  "ipCode": "2006967e-81c3-4756-9330-fc2ffe7067c2",
                                                  "stock": 100,
                                                  "description": "테스트 용 상품 입니다",
                                                  "salePrice": 9000,
                                                  "price": 10000,
                                                  "options": [
                                                    {
                                                      "name": "테스트 상품 옵션 1",
                                                      "sortOrder": 1,
                                                      "price": 10000,
                                                      "salePrice": 9000,
                                                      "stock": 100,
                                                      "status": "ON_SALE"
                                                    }
                                                  ]
                                                }
                                                """
                                )
                        ),
                        // 파일
                        @SchemaProperty(
                                name = "images",
                                array = @ArraySchema(schema = @Schema(type = "string", format = "binary"))
                        )
                }
        )
)


@ApiResponses({
        @ApiResponse(responseCode = "200", description = "상품 등록 성공")
})
public @interface ProductAddDocs {
}
