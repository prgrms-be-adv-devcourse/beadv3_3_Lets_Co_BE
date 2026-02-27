package co.kr.assistant.config;

import co.kr.assistant.interceptor.SessionInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.concurrent.Executor;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final SessionInterceptor sessionInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(sessionInterceptor)
                .addPathPatterns("/assistant/**") // 모든 어시스턴트 API에 적용
                .excludePathPatterns("/assistant/start"); // 세션 생성 API는 제외
    }

    // [추가됨] AI 호출 전용 커스텀 스레드 풀 빈 등록 (스레드 풀 고갈 방지)
    @Bean(name = "aiThreadPoolTaskExecutor")
    public Executor aiThreadPoolTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);  // 기본 유지 스레드 수
        executor.setMaxPoolSize(100);  // 트래픽 폭주 시 최대 확장 가능한 스레드 수
        executor.setQueueCapacity(50); // 스레드가 꽉 찼을 때 대기하는 큐의 크기
        executor.setThreadNamePrefix("AI-Chat-"); // 로그 추적을 위한 스레드 이름 접두사
        executor.initialize();
        return executor;
    }
}