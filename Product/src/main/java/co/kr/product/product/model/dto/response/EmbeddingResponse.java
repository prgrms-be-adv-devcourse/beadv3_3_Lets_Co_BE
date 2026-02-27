package co.kr.product.product.model.dto.response;

import java.util.List;

public record EmbeddingResponse(
        String status,
        List<Float> vector
) {
}
