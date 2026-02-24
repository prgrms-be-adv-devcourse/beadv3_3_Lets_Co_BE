package co.kr.user.util;

import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 이미지 파일의 보안 검증 및 무결성 처리를 담당하는 유틸리티 클래스입니다.
 */
@Component
public class FileUtil {
    private final Tika tika = new Tika();

    /**
     * 화이트리스트 기반의 허용된 이미지 MIME 타입 목록입니다.
     */
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    /**
     * 이미지 파일을 검증하고, 메타데이터 제거를 위해 재인코딩 프로세스를 거칩니다.
     *
     * @param file 클라이언트로부터 업로드된 파일
     * @return 보안 처리가 완료된 이미지 바이트 배열
     * @throws IOException 파일 읽기/쓰기 오류 발생 시
     */
    public byte[] validateAndProcessImage(MultipartFile file) throws IOException {
        // 1단계: 파일의 헤더(매직 바이트)를 직접 확인하여 확장자 위조 여부를 검사합니다.
        if (!verifyMagicBytes(file)) {
            throw new IllegalArgumentException("변조되었거나 지원하지 않는 파일 시그니처입니다.");
        }

        // 2단계: Apache Tika 라이브러리를 사용하여 실제 파일 바이너리의 MIME 타입을 분석합니다.
        if (!verifyMimeType(file)) {
            throw new IllegalArgumentException("허용되지 않는 이미지 형식입니다.");
        }

        // 3단계: 이미지를 새 도화지에 다시 그림으로써 악성 코드 및 EXIF 정보를 제거합니다.
        return reprocessImage(file);
    }

    /**
     * 파일의 시작 부분(시그니처)을 읽어 특정 이미지 형식과 일치하는지 확인합니다.
     */
    private boolean verifyMagicBytes(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] signature = new byte[12];
            int read = is.read(signature);
            if (read < 12) return false;

            // JPEG (FF D8 FF) 검사
            if ((signature[0] & 0xFF) == 0xFF && (signature[1] & 0xFF) == 0xD8 && (signature[2] & 0xFF) == 0xFF) return true;
            // PNG (89 50 4E 47) 검사
            if ((signature[0] & 0xFF) == 0x89 && (signature[1] & 0xFF) == 0x50 && (signature[2] & 0xFF) == 0x4E && (signature[3] & 0xFF) == 0x47) return true;
            // GIF (47 49 46 38) 검사
            if ((signature[0] & 0xFF) == 0x47 && (signature[1] & 0xFF) == 0x49 && (signature[2] & 0xFF) == 0x46 && (signature[3] & 0xFF) == 0x38) return true;
            // WebP (RIFF...WEBP) 검사
            if ((signature[0] & 0xFF) == 0x52 && (signature[1] & 0xFF) == 0x49 && (signature[2] & 0xFF) == 0x46 && (signature[3] & 0xFF) == 0x46) {
                return (signature[8] & 0xFF) == 0x57 && (signature[9] & 0xFF) == 0x45 && (signature[10] & 0xFF) == 0x42 && (signature[11] & 0xFF) == 0x50;
            }

            return false;
        }
    }

    /**
     * Tika 라이브러리를 통해 파일 내용을 분석하여 실제 MIME 타입을 추출합니다.
     */
    private boolean verifyMimeType(MultipartFile file) throws IOException {
        String mimeType = tika.detect(file.getInputStream());
        return ALLOWED_MIME_TYPES.contains(mimeType);
    }

    /**
     * 이미지를 메모리에 로드한 후 다시 인코딩하여 저장함으로써 잠재적 공격(폴리글럿 등)을 원천 차단합니다.
     */
    private byte[] reprocessImage(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                throw new IllegalArgumentException("이미지 데이터를 읽을 수 없습니다. 파일이 손상되었거나 형식이 잘못되었습니다.");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            String formatName = getFormatName(file);

            // 새 이미지 스트림으로 내보내며 불필요한 메타데이터를 모두 소거합니다.
            ImageIO.write(originalImage, formatName, os);
            return os.toByteArray();
        }
    }

    /**
     * 파일의 Content-Type을 기반으로 적절한 이미지 포맷 명칭을 반환합니다.
     */
    private String getFormatName(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return "png";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        if (contentType.contains("gif")) return "gif";
        if (contentType.contains("webp")) return "webp";
        return "png";
    }
}