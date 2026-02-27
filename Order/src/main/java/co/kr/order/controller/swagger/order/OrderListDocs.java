package co.kr.order.controller.swagger.order;

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
        summary = "주문 목록 조회",
        description = """
                사용자의 주문 목록을 페이징으로 조회한다.

                - 사용자 idx 필수 (헤더)
                - 기본 10개씩, 생성일 내림차순 정렬
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
                example = "10"
        ),
        @Parameter(
                name = "sort",
                in = ParameterIn.QUERY,
                description = "정렬 기준 (예: createdAt,desc)",
                example = "createdAt,desc"
        )
})

// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "주문 목록 조회 성공")
})
public @interface OrderListDocs {
}
