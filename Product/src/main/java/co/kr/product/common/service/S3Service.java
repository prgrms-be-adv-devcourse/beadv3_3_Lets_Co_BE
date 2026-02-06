package co.kr.product.common.service;


import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${custom.aws.s3.bucket}")
    private String bucketName;

    @Value("${custom.aws.s3.presigned-duration}")
    private String presignedDuration;

    @Value("${custom.aws.s3.product-prefix}")
    private String productPrefix;

    public String uploadFile(MultipartFile file){

        // TODO !!! 입력받은 파일 유효성 검사 !!!!매우중요!!!!!

        // key : s3 내 "폴더/파일명" (랜덤으로 생성)
        String key = productPrefix +"/" + UUID.randomUUID().toString();

        try {
            // 입력용 오브젝트 요청
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromBytes(file.getBytes()));
        }catch (Exception e){
            throw new IllegalArgumentException("파일 업로드에 실패했습니다");
        }


        return key;
    }

    public String getFileUrl(String key){
        // 파일을 찾아오기 위한 값
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                // s3 버킷 이름
                .bucket(bucketName)
                // key (폴더/~~ /파일명)
                .key(key)
                .build();

        // 이후 추가 서명이나 인증없이 실행 할 수 있도록 미리 서명을 요구하는 요청
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                // 유효시간 입력
                .signatureDuration(Duration.ofMinutes(Long.parseLong(presignedDuration)))
                // 위에서 만든 파일 입력
                .getObjectRequest(getObjectRequest)
                .build();

        // 사전 서명 된 object 가져오기
        PresignedGetObjectRequest request = s3Presigner.presignGetObject(presignRequest);

        return request.url().toString();
    }



}
