package co.kr.assistant.client;

import co.kr.assistant.model.dto.rag.RagReq;
import co.kr.assistant.model.dto.rag.RagRes; // 변경된 DTO 사용
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RagServiceClient {

    private final WebClient webClient;

    public RagServiceClient(WebClient.Builder webClientBuilder,
                            @Value("${rag.server.url:http://localhost:8077}") String baseUrl) {
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Python RAG 서버에 지식 검색 및 의도 분류 요청을 보냅니다.
     * 반환 타입이 List<RagRes>에서 RagRes로 변경되었습니다.
     */
    public RagRes searchKnowledge(String query, int topK) {
        RagReq request = new RagReq(query, topK);

        return webClient.post()
                .uri("/search")
                .bodyValue(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, response ->
                        response.bodyToMono(String.class).flatMap(errorBody -> {
                            System.err.println("Python Server Error Body: " + errorBody);
                            return Mono.error(new RuntimeException("RAG 서버 요청 실패: " + errorBody));
                        })
                )
                .bodyToMono(RagRes.class) // 단일 객체로 수신
                .block();
    }

    public void triggerSync() {
        webClient.post()
                .uri("/sync")
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(result -> System.out.println("RAG Sync Started: " + result));
    }
}