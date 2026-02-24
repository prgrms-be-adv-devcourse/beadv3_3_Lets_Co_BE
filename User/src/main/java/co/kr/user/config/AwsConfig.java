package co.kr.user.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS SDK v2를 사용하여 S3 통신에 필요한 Bean들을 생성하고 관리하는 설정 클래스입니다.
 */
@Configuration
public class AwsConfig {

    @Value("${custom.aws.region}")
    private String region;

    @Value("${custom.aws.credentials.access-key}")
    private String accessKey;

    @Value("${custom.aws.credentials.secret-key}")
    private String secretKey;

    /**
     * AWS 인증 정보를 제공하는 Provider를 생성합니다.
     * 배포 환경(IAM Role), 시스템 환경변수, 로컬 설정값 순으로 인증을 시도하는 체인 방식을 채택합니다.
     * * @return 우선순위에 따라 구성된 AwsCredentialsProvider 객체
     */
    @Bean
    public AwsCredentialsProvider customCredentialsProvider() {
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create()) // 1순위: EC2 인스턴스 프로파일(IAM Role)
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create()) // 2순위: 시스템 환경변수
                .addCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey))) // 3순위: application.yml에 명시된 고정 키
                .build();
    }

    /**
     * S3 버킷과 상호작용(업로드/삭제 등)하기 위한 표준 S3 클라이언트를 생성합니다.
     * * @return 구성된 S3Client Bean
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(customCredentialsProvider())
                .build();
    }

    /**
     * S3 객체에 대한 임시 보안 URL(Presigned URL)을 생성하기 위한 전용 객체입니다.
     * * @return S3Presigner Bean
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(customCredentialsProvider())
                .build();
    }
}