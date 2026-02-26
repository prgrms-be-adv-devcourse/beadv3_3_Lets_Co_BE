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

/**
 * 외부 RAG 서버와 통신하는 클라이언트 클래스입니다.
 * 타임아웃 및 예외 처리를 통해 시스템 안정성을 강화했습니다.
 */
@Slf4j
@Component
public class RagServiceClient {

    private final WebClient webClient;

    // RAG 서버 응답 대기 시간 (기본 10초, 설정 파일에서 조절 가능)
    @Value("${rag.server.timeout:10}")
    private int timeoutSeconds;

    public RagServiceClient(WebClient.Builder webClientBuilder,
                            @Value("${rag.server.url:http://localhost:8077}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Python RAG 서버에 지식 검색 및 의도 분류 요청을 보냅니다.
     * 무한정 대기를 방지하기 위해 타임아웃을 설정하고, 에러 발생 시 로그를 남긴 후 빈 결과를 반환합니다.
     */
    public RagRes searchKnowledge(String query, int topK) {
        RagReq request = new RagReq(query, topK);

        try {
            return webClient.post()
                    .uri("/search")
                    .bodyValue(request)
                    .retrieve()
                    // 4xx 및 5xx 에러를 통합하여 로그를 남기고 서비스 중단을 방지합니다.
                    .onStatus(HttpStatusCode::isError, response ->
                            response.bodyToMono(String.class).flatMap(errorBody -> {
                                log.error("RAG 서버 응답 오류: {}", errorBody);
                                return Mono.error(new RuntimeException("RAG Server Error"));
                            })
                    )
                    .bodyToMono(RagRes.class)
                    .timeout(Duration.ofSeconds(timeoutSeconds)) // 응답 타임아웃 설정
                    .onErrorResume(e -> {
                        // 통신 장애나 타임아웃 시 null 대신 빈 결과를 반환하여 대화 흐름을 유지합니다.
                        log.error("RAG 서버 통신 장애: {}", e.getMessage());
                        return Mono.empty();
                    })
                    .block(); // 동기 처리를 위해 block 사용
        } catch (Exception e) {
            log.error("RAG 서비스 호출 중 예외 발생: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 특정 아이템에 대해 파이썬 RAG 서버(FAISS) 및 검색 서버(ES) 동기화를 요청합니다.
     * @param sourceId 동기화할 대상의 ID (예: "Products:123", "Customer_Service:456")
     */
    public void triggerSync(String sourceId) {
        webClient.post()
                .uri("/sync-item/{sourceId}", sourceId) // 파이썬 서버의 /sync-item/{source_id}와 일치시킴
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(5))
                .subscribe(
                        result -> log.info("아이템 동기화 요청 성공: {} -> {}", sourceId, result),
                        error -> log.error("아이템 동기화 요청 실패: {} -> {}", sourceId, error.getMessage())
                );
    }
}