package co.kr.product.product.controller.swagger.ip;


import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.lang.annotation.*;

@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Operation(
        summary = "IP 전체 목록 조회",
        description = """
                IP 전체 조회
                
                - 순서 및 레벨 별 정렬 ㅇ
                - 별도의 요청 데이터 x
                """
)


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "IP 조회 성공")
})
public @interface IpListDocs {
}
