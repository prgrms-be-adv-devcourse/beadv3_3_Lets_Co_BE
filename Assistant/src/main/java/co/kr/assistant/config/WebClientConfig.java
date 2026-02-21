package co.kr.assistant.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.HttpProtocol;
import reactor.netty.http.client.HttpClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient.Builder webClientBuilder() {
        // HTTP/2 (h2c) 프로토콜 활성화
        HttpClient httpClient = HttpClient.create()
                .protocol(HttpProtocol.H2C, HttpProtocol.HTTP11); // h2c 우선 시도

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }
}