package co.kr.user.config; // 패키지 경로를 프로젝트에 맞게 수정

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.*;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class AwsConfig {

    @Value("${custom.aws.region}")
    private String region;

    @Value("${custom.aws.credentials.access-key}")
    private String accessKey;

    @Value("${custom.aws.credentials.secret-key}")
    private String secretKey;

    @Bean
    public AwsCredentialsProvider customCredentialsProvider() {
        // AWS SDK v2 권장 방식: 여러 인증 수단을 체인으로 묶음
        return AwsCredentialsProviderChain.builder()
                .addCredentialsProvider(InstanceProfileCredentialsProvider.create()) // IAM Role (배포용)
                .addCredentialsProvider(EnvironmentVariableCredentialsProvider.create()) // 환경변수
                .addCredentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey))) // 직접 주입 (로컬용)
                .build();
    }

    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(customCredentialsProvider())
                .build();
    }

    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(customCredentialsProvider())
                .build();
    }
}