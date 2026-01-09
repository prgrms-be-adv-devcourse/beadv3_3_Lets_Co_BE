package co.kr.product.seller.model.dto;

public record ProductListRequest(
        String filter,
        String category,
        String search

) {
}
