package co.kr.product.common.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    // 임시
    private String aiServerBaseUrl = "http://localhost:8000/";


    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder
                .baseUrl(aiServerBaseUrl) // 1. 기본 URL 설정
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE) // 2. 기본 헤더 설정
                .build();
    }
}