package co.kr.product.product.model.dto.response;

public record ProductInfoResponse(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
