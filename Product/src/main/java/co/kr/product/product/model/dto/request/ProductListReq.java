package co.kr.product.product.model.dto.request;

import jakarta.validation.constraints.Size;

public record ProductListReq(
        // String filter,

        // 검색어는 없을 수 도 있기에 nullable
        @Size(max = 100, message = "검색어는 100자를 넘을 수 없습니다.")
        String search,

        @Size(max = 20, message = "카테고리는 20자를 넘을 수 없습니다.")
        String category

) {
}
