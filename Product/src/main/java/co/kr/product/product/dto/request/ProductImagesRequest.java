package co.kr.product.product.dto.request;

public record ProductImagesRequest(
        Long imageIdx,
        String url,
        Integer sortOrder,
        Boolean isThumbnail
) {
}
