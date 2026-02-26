package co.kr.product.product.service;

import co.kr.product.product.model.dto.response.EmbeddingResponse;

import java.util.List;

public interface EmbeddingService {
    List<Float> getEmbedded(String keyword);
}
