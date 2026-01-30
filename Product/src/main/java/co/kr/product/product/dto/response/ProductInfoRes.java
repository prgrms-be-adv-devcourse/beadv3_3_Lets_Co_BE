package co.kr.product.product.dto.response;

public record ProductInfoRes(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
