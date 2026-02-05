package co.kr.product.common.configuration;


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

    @Value("${custom.aws.s3.bucket}")
    private String bucket;

    @Value("${custom.aws.s3.presigned-duration}")
    private Long presignedDuration;


    @Bean
    public AwsCredentialsProviderChain customCredentialsProvider(){
        return AwsCredentialsProviderChain.builder()
                .credentialsProviders(
                        // IAM ROLE 확인
                        InstanceProfileCredentialsProvider.builder().build(),
                        // 환경 변수 확인
                        EnvironmentVariableCredentialsProvider.create(),
                        // 이 마저도 안되면 직접 주입해보기
                        StaticCredentialsProvider.create(
                                AwsBasicCredentials.create(accessKey,secretKey))
                ).build();
    }

    @Bean
    public S3Client s3Client(){


        return S3Client.builder()
            .region(Region.of(region))

                // 신분증 내놔
            .credentialsProvider(
                    // 0, 직접 만듬 ㅇㅅㅇ
                    // IAM 확인 (배포 환경) >  환경변수 확인(로컬) > 직접 주입
                    customCredentialsProvider())

                    // 아래로는 기존 사용을 고려했던 후보군

                    // 1. DefaultCredentialsProvider > 순서대로 필요한 키를 찾음
                    //                            > 시스템속성, 환경변수 ~~ 로컬파일, ec2 자체 IAM 등
                    // 환경변수를 먼저 찾아보기에 accessKey, secretKey가 존재하면 바로 AwsBasicCredentials을 통한
                    // 영구키를 만들것
                    // DefaultCredentialsProvider.create()는 더이상 사용 안함.
                    // DefaultCredentialsProvider.builder().build()

                    // 2. EnvironmentVariableCredentialsProvider > 환경 변수만 찾음 >  access, secret키 찾아 씀
                    // EnvironmentVariableCredentialsProvider.create()

                    // 3. StaticCredentialsProvider > 위처럼 자동으로 가져오는 것이 아닌, 직접 주입
                    /*
                    StaticCredentialsProvider.create(
                            // AwsSessionCredentials >> 서비스 인증 용 세션 토큰 제공(임시 엑세스 권한)
                            // AwsBasicCredentials >> 영구 키, accessKey, secretKey만으로 인증
                            AwsBasicCredentials.create(accessKey, secretKey)
                    ))
                    */


                .build();
    }

    //  Presigned URL 생성용 객체
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(customCredentialsProvider())
                .build();
    }
    
}
