package co.kr.costomerservice.common.dto.request;

import java.util.List;

public record ProductIdxsRequest(
        List<Long> productIdxs
) {

}
