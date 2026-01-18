package co.kr.costomerservice.common.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProductIdxsRequest(

        @NotNull(message = "상품 ID 리스트는 null일 수 없습니다.")
        @NotEmpty(message = "최소 1개 이상의 상품 ID가 필요합니다.")
        List<Long> productIdxs
) {

}
