package co.kr.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Spring Boot 애플리케이션의 진입점(Entry Point) 클래스입니다.
 * 애플리케이션의 설정과 실행을 담당합니다.
 */
@EnableAsync // 비동기 처리를 활성화합니다. (@Async 어노테이션 사용 가능)
@EnableJpaAuditing // JPA Auditing 기능을 활성화합니다. (엔티티의 생성일/수정일 자동 기록 등)
@SpringBootApplication // Spring Boot 자동 설정, 컴포넌트 스캔, 설정 클래스 선언을 포함하는 메타 어노테이션입니다.
public class UserApplication {

    /**
     * 메인 메서드: 애플리케이션을 실행합니다.
     * @param args 커맨드 라인 인자
     */
    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }
}