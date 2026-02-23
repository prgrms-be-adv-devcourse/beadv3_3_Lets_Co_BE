package co.kr.user.service.impl; // 소문자 패키지 권장

import co.kr.user.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.time.Duration;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${custom.aws.s3.bucket}")
    private String bucketName;

    @Value("${custom.aws.s3.presigned-duration}")
    private Long presignedDuration;

    @Override
    public void putObject(byte[] data, String key, String contentType) {
        try {
            // Consumer Builder 방식 적용 (Error 4 해결)
            s3Client.putObject(b -> b.bucket(bucketName)
                            .key(key)
                            .contentType(contentType),
                    RequestBody.fromBytes(data));
        } catch (Exception e) {
            log.error("S3 저장 실패: {}", e.getMessage());
            throw new IllegalArgumentException("파일 저장소 업로드 중 오류가 발생했습니다.");
        }
    }

    @Override
    public void deleteObject(String key) {
        try {
            s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        } catch (Exception e) {
            log.warn("S3 삭제 실패: {}", e.getMessage());
        }
    }

    @Override
    public String getPresignedUrl(String key) {
        try {
            // Consumer Builder 방식 적용
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