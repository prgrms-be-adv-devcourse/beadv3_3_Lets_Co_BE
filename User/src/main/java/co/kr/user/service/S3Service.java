package co.kr.user.service;

/**
 * S3 저장소와의 순수 통신을 담당하는 인터페이스
 */
public interface S3Service {
    /**
     * 바이트 데이터를 S3에 업로드
     */
    void putObject(byte[] data, String key, String contentType);

    /**
     * S3에서 객체 삭제
     */
    void deleteObject(String key);

    /**
     * 특정 Key에 대한 Presigned URL 발급
     */
    String getPresignedUrl(String key);
}