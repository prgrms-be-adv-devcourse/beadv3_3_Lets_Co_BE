package co.kr.product.product.dto.response;

public record ProductInfoResponse(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
