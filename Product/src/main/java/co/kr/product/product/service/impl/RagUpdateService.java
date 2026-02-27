package co.kr.product.product.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;

@Slf4j
@Service
public class RagUpdateService {

    private final WebClient webClient;

    // RAG 서버 응답 대기 시간 (기본 10초, 설정 파일에서 조절 가능)
    @Value("${custom.rag.timeout:10}")
    private int timeoutSeconds;

    @Value("${custom.rag.prefix}")
    private String productPrefix;

    public RagUpdateService(WebClient.Builder webClientBuilder,
                            @Value("${custom.rag.url}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * 특정 아이템에 대해 파이썬 RAG 서버(FAISS) 및 검색 서버(ES) 동기화를 요청합니다.
     * @param productsIdx
     */
    public void triggerSync(Long productsIdx) {
        String sourceId = productPrefix + productsIdx;

        webClient.post()
                .uri("/sync-item/{sourceId}", sourceId) // 파이썬 서버의 /sync-item/{source_id}와 일치시킴
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSeconds))
                .subscribe(
                        result -> log.info("아이템 동기화 요청 성공: {} -> {}", sourceId, result),
                        error -> log.error("아이템 동기화 요청 실패: {} -> {}", sourceId, error.getMessage())
                );
    }
}
