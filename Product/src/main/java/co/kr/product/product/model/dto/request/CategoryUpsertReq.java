package co.kr.product.product.model.dto.request;

public record CategoryUpsertReq(
        String categoryName,
        String parentCode
) {
}
