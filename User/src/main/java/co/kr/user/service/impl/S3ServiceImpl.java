package co.kr.user.service.impl;

import co.kr.user.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

/**
 * AWS S3 SDK를 사용하여 실제 저장소 작업을 수행하는 서비스 클래스입니다.
 * 파일 업로드, 삭제 및 보안 접근을 위한 Presigned URL 생성을 담당합니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    /**
     * application.yml 설정을 통해 주입받는 S3 버킷 이름입니다.
     */
    @Value("${custom.aws.s3.bucket}")
    private String bucketName;

    /**
     * Presigned URL의 유효 기간(분 단위)입니다.
     */
    @Value("${custom.aws.s3.presigned-duration}")
    private Long presignedDuration;

    /**
     * S3 버킷에 바이너리 데이터를 업로드합니다.
     *
     * @param data 업로드할 파일의 바이트 배열
     * @param key S3 내 저장 경로 및 파일명
     * @param contentType 파일의 MIME 타입 (예: image/png)
     */
    @Override
    public void putObject(byte[] data, String key, String contentType) {
        try {
            // AWS SDK v2의 Consumer Builder 방식을 사용하여 객체를 업로드합니다.
            s3Client.putObject(b -> b.bucket(bucketName)
                            .key(key)
                            .contentType(contentType),
                    RequestBody.fromBytes(data));
        } catch (Exception e) {
            log.error("S3 저장 실패: {}", e.getMessage());
            throw new IllegalArgumentException("파일 저장소 업로드 중 오류가 발생했습니다.");
        }
    }

    /**
     * S3 버킷에서 특정 키(경로)를 가진 객체를 삭제합니다.
     *
     * @param key 삭제할 객체의 S3 키
     */
    @Override
    public void deleteObject(String key) {
        try {
            s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        } catch (Exception e) {
            // 삭제 실패는 로그만 남기고 서비스 흐름을 방해하지 않도록 처리합니다.
            log.warn("S3 삭제 실패: {}", e.getMessage());
        }
    }

    /**
     * 비공개 S3 객체에 일정 시간 동안 접근할 수 있는 서명된 URL을 생성합니다.
     *
     * @param key 접근할 객체의 S3 키
     * @return 생성된 Presigned URL 문자열, 실패 시 null 반환
     */
    @Override
    public String getPresignedUrl(String key) {
        try {
            return s3Presigner.presignGetObject(b -> b
                            .signatureDuration(Duration.ofMinutes(presignedDuration))
                            .getObjectRequest(r -> r.bucket(bucketName).key(key)))
                    .url().toString();
        } catch (Exception e) {
            log.error("URL 생성 실패: {}", e.getMessage());
            return null;
        }
    }
}