package co.kr.assistant.client;

import co.kr.assistant.model.dto.rag.RagReq;
import co.kr.assistant.model.dto.rag.RagRes;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class RagServiceClient {

    private final WebClient webClient;

    public RagServiceClient(WebClient.Builder webClientBuilder,
                            @Value("${rag.server.url:http://localhost:8077}") String baseUrl) {
        // WebClientConfig에서 생성된 builder를 사용하여 baseUrl 설정
        this.webClient = webClientBuilder.baseUrl(baseUrl).build();
    }

    /**
     * Python RAG 서버에 지식 검색 요청을 보냅니다.
     */
    public List<RagRes> searchKnowledge(String query, int topK) {
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
                .bodyToFlux(RagRes.class)
                .collectList()
                .block();
    }

    /**
     * Python RAG 서버에 DB 동기화를 요청합니다.
     */
    public void triggerSync() {
        webClient.post()
                .uri("/sync") // Python 서버의 POST /sync 호출
                .retrieve()
                .bodyToMono(String.class)
                .subscribe(result -> System.out.println("RAG Sync Started: " + result));
    }
}