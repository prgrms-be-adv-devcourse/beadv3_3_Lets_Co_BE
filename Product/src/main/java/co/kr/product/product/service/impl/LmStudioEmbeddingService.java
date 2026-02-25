package co.kr.product.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service

public class LmStudioEmbeddingService {

    private final WebClient webClient;


    private String modelName = "gpustack/bge-m3-GGUF";


    private String embeddingPath = "/v1/embeddings";


    public LmStudioEmbeddingService(WebClient.Builder webClientBuilder) {
        String baseUrl = "http://localhost:1234";
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
        log.info("LM Studio Embedding Service initialized with URL: {}", baseUrl);
    }

    public List<Float> getVectorEmbedding(String query) {
        // 1. 요청 본문 구성 (OpenAI API 규격 준수)
        // Ollama는 "prompt"를 쓰지만, LM Studio(OpenAI style)는 "input"을 사용합니다.
        Map<String, Object> requestBody = Map.of(
                "model", modelName,
                "input", query
        );

        try {
            // 2. API 호출
            Map<?, ?> response = webClient.post()
                    .uri(embeddingPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(requestBody))
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            // 3. 응답 파싱 (구조: { "data": [ { "embedding": [...] } ], ... })
            return parseEmbeddingResponse(response);

        } catch (Exception e) {
            log.error("Error calling LM Studio API for query: {}", query, e);
            return Collections.emptyList();
        }
    }

    /**
     * OpenAI 호환 응답 구조에서 임베딩 벡터를 안전하게 추출합니다.
     */
    private List<Float> parseEmbeddingResponse(Map<?, ?> response) {
        if (response == null || !response.containsKey("data")) {
            log.warn("Invalid response from LM Studio: 'data' field missing.");
            return Collections.emptyList();
        }

        try {
            // "data" 필드는 리스트 형태입니다.
            List<?> dataList = (List<?>) response.get("data");
            if (dataList == null || dataList.isEmpty()) {
                return Collections.emptyList();
            }

            // 첫 번째 객체({ "embedding": [...] })를 가져옵니다.
            Map<?, ?> firstItem = (Map<?, ?>) dataList.get(0);
            if (firstItem == null || !firstItem.containsKey("embedding")) {
                return Collections.emptyList();
            }

            // "embedding" 필드 추출 및 형변환 (Double -> Float)
            List<?> embeddingList = (List<?>) firstItem.get("embedding");
            if (embeddingList != null) {
                return embeddingList.stream()
                        .filter(item -> item instanceof Number)
                        .map(item -> ((Number) item).floatValue())
                        .toList();
            }
        } catch (ClassCastException e) {
            log.error("Failed to parse LM Studio response structure.", e);
        }

        return Collections.emptyList();
    }
}

