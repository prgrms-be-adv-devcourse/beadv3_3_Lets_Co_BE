package co.kr.costomerservice.common.dto.response;

public record ProductInfoResponse(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
