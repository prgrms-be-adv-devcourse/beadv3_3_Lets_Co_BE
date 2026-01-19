package co.kr.user;

import co.kr.user.DAO.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * [스프링 부트 메인 애플리케이션]
 * 이 프로젝트의 진입점(Entry Point)입니다.
 * main 메서드를 실행하면 내장 톰캣(Tomcat) 서버가 켜지고 애플리케이션이 동작합니다.
 */
@EnableAsync // [비동기 처리 활성화] 이메일 발송(@Async) 기능을 사용하기 위해 필수적인 설정입니다.
@EnableJpaAuditing // [JPA Auditing 활성화] 엔티티의 생성일(@CreatedDate), 수정일(@LastModifiedDate) 자동 주입 기능을 켭니다.
@SpringBootApplication // [스프링 부트 자동 설정] ComponentScan, EnableAutoConfiguration 등을 포함하는 핵심 어노테이션입니다.
public class UserApplication {

    public static void main(String[] args) {
        // 애플리케이션을 실행합니다. (내장 웹 서버 구동, 빈 등록 등)
        SpringApplication.run(UserApplication.class, args);
    }

    /**
     * [DB 연결 테스트용 Runner]
     * 애플리케이션이 시작된 직후(Run 시점) 자동으로 실행되는 메서드입니다.
     * 서버가 켜질 때 DB 연결이 잘 되었는지 콘솔 로그로 바로 확인할 수 있어 개발 시 유용합니다.
     * * @param userRepository 테스트를 위해 주입받은 사용자 리포지토리
     * @return 실행할 로직을 담은 CommandLineRunner 객체
     */
    @Bean
    public CommandLineRunner testDBConnection(UserRepository userRepository) {
        return args -> {
            System.out.println("=============================================");
            System.out.println("🔍 [DB 연결 테스트] 데이터 확인 시작");

            // DB에 'select count(*)' 쿼리를 날려 연결 상태를 확인합니다.
            long count = userRepository.count();
            System.out.println("📊 총 유저 수: " + count + "명");

            if (count > 0) {
                // 데이터가 있다면 전체 조회하여 로그 출력 (개발 단계에서만 사용 권장)
                userRepository.findAll().forEach(user ->
                        System.out.println("   👤 유저 발견: " + user.getID())
                );
            } else {
                System.out.println("   ⚠️ 데이터가 없습니다! (연결된 DB가 비어있거나 초기화됨)");
            }
            System.out.println("=============================================");
        };
    }
}