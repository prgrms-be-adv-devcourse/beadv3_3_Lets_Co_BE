package co.kr.customerservice.common.model.dto.response;

public record ProductInfoResponse(
        Long productIdx,
        String productCode,
        String name,

        String imageUrl
) {
}
