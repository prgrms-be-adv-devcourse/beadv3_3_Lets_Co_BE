package co.kr.product.common.service;


import co.kr.product.product.model.dto.response.ImageUploadRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
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

    public ImageUploadRes uploadFile(MultipartFile file){

        // TODO !!! 입력받은 파일 유효성 검사 !!!!매우중요!!!!!

        // s3 에 저장 될 파일명 (랜덤으로 생성)
        String storedFileName = UUID.randomUUID().toString();
        String filePath = productPrefix +"/";
        // key : s3 내 "폴더/파일명"
        String key = filePath + storedFileName;

        try {
            // 입력용 오브젝트 요청
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

        }catch (Exception e){
            throw new IllegalArgumentException("파일 업로드에 실패했습니다");
        }

        return new ImageUploadRes(
                file.getOriginalFilename(),
                storedFileName,
                filePath,
                file.getContentType(),
                key
        );
    }

    // S3에 업로드
    public List<ImageUploadRes> uploadFiles(List<MultipartFile> files){

        if (files.isEmpty()){
            return List.of();
        }

        List<ImageUploadRes> res = new java.util.ArrayList<>();

        try{
            for(MultipartFile file : files ){
                if(file.isEmpty()) continue;
                res.add(uploadFile(file));
            }
        }catch (Exception e){

            // 자동 트랜잭션이 안되기에 직접 삭제해야함.
            // 오류 날 시 복구
            List<String> keys = res.stream()
                                    .map( dto -> dto.key())
                                    .toList();
            deleteFiles(keys);

            throw new IllegalArgumentException("파일 저장 중 오류 발생");
        }

        // 생성 된 keys 반환
        return res;
    }

    // S3 내 파일 삭제
    public void deleteFiles(List<String> keys){
        if (keys.isEmpty()) return;
        try {
            // 반복문으로 지우든, s3 deleteObjects(배열)로 한방에 지우든 구현
            keys.forEach(key -> s3Client.deleteObject(b -> b.bucket(bucketName).key(key)));
        } catch (Exception e) {
            log.warn("삭제 실패 : " + e);
        }
    }


    // 단일 파일 조회
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

    // 여러 파일 조회
    public List<String> getFileUrls(List<String> keys){

        if(keys.isEmpty()){
            return List.of();
        }

        // url 리스트 반환
        return keys.stream()
                .map(key -> getFileUrl(key))
                .toList();

    }


}
