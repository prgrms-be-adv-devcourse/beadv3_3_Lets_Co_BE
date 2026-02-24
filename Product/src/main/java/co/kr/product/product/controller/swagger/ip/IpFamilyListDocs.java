package co.kr.product.product.controller.swagger.ip;


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
        summary = "특정 IP의 부모/자식 조회",
        description = """
                IP 부모/자식 조회
                
                - 사용자 idx 필요 x
                 > 회원/비회원 관계 없이 접근 가능
                
                """
)

@Parameters({
        @Parameter(
                name = "categoryCode",
                in = ParameterIn.PATH,
                description = "조회 할 IP의 Code",
                required = true,
                example = "2006967e-81c3-4756-9330-fc2ffe7067c2"
        )
})


// 예시 응답들 설정
@ApiResponses({
        @ApiResponse(responseCode = "200", description = "IP 부모/자식 조회 성공")
})
public @interface IpFamilyListDocs {
}
