package co.kr.assistant.client;

import co.kr.assistant.model.dto.rag.RagReq;
import co.kr.assistant.model.dto.rag.RagRes;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Slf4j
@Component
public class RagServiceClient {

    private final WebClient webClient;

    @Value("${rag.server.timeout:10}")
    private int timeoutSeconds;

    public RagServiceClient(WebClient.Builder webClientBuilder,
                            @Value("${rag.server.url:http://localhost:8077}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    // 파라미터를 통째로 RagReq 객체로 받도록 변경
    public RagRes searchKnowledge(RagReq request) {
        try {
            return webClient.post()
                    .uri("/search")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("RAG 서버 응답 오류: {}", errorBody);
                                return Mono.error(new RuntimeException("RAG Server Error"));
                            })
                    )
                    .bodyToMono(RagRes.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .onErrorResume(e -> {
                        log.error("RAG 서버 통신 장애: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block();
        } catch (Exception e) {
            log.error("RAG 서비스 호출 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    public void triggerSync(String sourceId) {
        webClient.post()
                .uri("/sync-item/{sourceId}", sourceId)
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        result -> log.info("아이템 동기화 요청 성공: {} -> {}", sourceId, result),
                        error -> log.error("아이템 동기화 요청 실패: {} -> {}", sourceId, error.getMessage())
                );
    }
}