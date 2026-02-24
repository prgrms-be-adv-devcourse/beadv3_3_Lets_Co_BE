package co.kr.product.product.model.dto.response;

public record CategoryRes(
        String categoryCode,
        String categoryName,
        // 임시
        String path,
        String parentCode
) {
}
