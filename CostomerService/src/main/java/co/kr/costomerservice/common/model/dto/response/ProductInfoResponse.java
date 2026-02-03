package co.kr.costomerservice.common.model.dto.response;

public record ProductInfoResponse(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
