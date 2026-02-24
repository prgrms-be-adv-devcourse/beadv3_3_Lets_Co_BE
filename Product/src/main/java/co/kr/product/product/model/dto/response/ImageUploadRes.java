package co.kr.product.product.model.dto.response;

import org.springframework.web.multipart.MultipartFile;

public record ImageUploadRes(
        String originalFileName,
        String storedFileName,
        String filePath,
        String fileType,
        String key

) {
}
