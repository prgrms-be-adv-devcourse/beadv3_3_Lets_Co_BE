package co.kr.product.product.service.impl;

import co.kr.product.product.model.dto.response.EmbeddingResponse;
import co.kr.product.product.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Collections;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class EmbeddingServiceImpl implements EmbeddingService {

    private final WebClient webClient;

    @Value("${custom.embedding.embeddingUrl}")
    private String embeddingPath;

    public EmbeddingServiceImpl(WebClient.Builder webClientBuilder,
                                @Value("${custom.embedding.baseUrl}") String baseUrl) {
        this.webClient = webClientBuilder
                .baseUrl(baseUrl)
                .build();
    }

    @Override
    public List<Float> getEmbedded(String keyword){

        // 1. 요청 본문 생성
        Map<String, Object> requestBody = Map.of(
                "q",keyword
        );

        // 2. 요청
        try{
            EmbeddingResponse response = webClient.post()
                    .uri(embeddingPath)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(EmbeddingResponse.class)
                    .block();

            if(!response.status().equals("success")){
                log.error("임베딩 요청 중 에러 발생. code1 검색어 : " + keyword);
            }
            return response.vector();

        }catch (Exception e){
            log.error("임베딩 요청 중 에러 발생. code2 검색어 : " + keyword);
            return Collections.emptyList();
        }

    }

}
