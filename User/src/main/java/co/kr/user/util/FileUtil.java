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

@Component
public class FileUtil {
    private final Tika tika = new Tika();
    // 지원하는 확장자: jpg, jpeg, png, gif, webp
    private static final List<String> ALLOWED_MIME_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    public byte[] validateAndProcessImage(MultipartFile file) throws IOException {
        // 1. 매직 바이트(파일 시그니처) 검사
        if (!verifyMagicBytes(file)) {
            throw new IllegalArgumentException("변조되었거나 지원하지 않는 파일 시그니처입니다.");
        }

        // 2. Apache Tika를 이용한 실제 MIME 타입 검증
        if (!verifyMimeType(file)) {
            throw new IllegalArgumentException("허용되지 않는 이미지 형식입니다.");
        }

        // 3. 이미지 재인코딩 (메타데이터 제거 및 폴리글럿 공격 무력화)
        return reprocessImage(file);
    }

    private boolean verifyMagicBytes(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            byte[] signature = new byte[12];
            int read = is.read(signature);
            if (read < 12) return false;

            // JPEG: FF D8 FF
            if ((signature[0] & 0xFF) == 0xFF && (signature[1] & 0xFF) == 0xD8 && (signature[2] & 0xFF) == 0xFF) return true;
            // PNG: 89 50 4E 47
            if ((signature[0] & 0xFF) == 0x89 && (signature[1] & 0xFF) == 0x50 && (signature[2] & 0xFF) == 0x4E && (signature[3] & 0xFF) == 0x47) return true;
            // GIF: 47 49 46 38 ('GIF8')
            if ((signature[0] & 0xFF) == 0x47 && (signature[1] & 0xFF) == 0x49 && (signature[2] & 0xFF) == 0x46 && (signature[3] & 0xFF) == 0x38) return true;
            // WebP: RIFF (52 49 46 46) + WEBP (57 45 42 50)
            if ((signature[0] & 0xFF) == 0x52 && (signature[1] & 0xFF) == 0x49 && (signature[2] & 0xFF) == 0x46 && (signature[3] & 0xFF) == 0x46) {
                return (signature[8] & 0xFF) == 0x57 && (signature[9] & 0xFF) == 0x45 && (signature[10] & 0xFF) == 0x42 && (signature[11] & 0xFF) == 0x50;
            }

            return false;
        }
    }

    private boolean verifyMimeType(MultipartFile file) throws IOException {
        // 클라이언트의 Content-Type이 아닌 파일 바이너리를 직접 분석
        String mimeType = tika.detect(file.getInputStream());
        return ALLOWED_MIME_TYPES.contains(mimeType);
    }

    private byte[] reprocessImage(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream()) {
            // TwelveMonkeys 라이브러리가 클래스패스에 있으면 WebP도 읽기 가능
            BufferedImage originalImage = ImageIO.read(is);
            if (originalImage == null) {
                throw new IllegalArgumentException("이미지 데이터를 읽을 수 없습니다. 파일이 손상되었거나 형식이 잘못되었습니다.");
            }

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            String formatName = getFormatName(file);

            // 새 도화지에 이미지를 다시 그림으로써 숨겨진 악성 코드와 메타데이터(EXIF 등)를 완전히 제거
            ImageIO.write(originalImage, formatName, os);
            return os.toByteArray();
        }
    }

    private String getFormatName(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) return "png";
        if (contentType.contains("jpeg") || contentType.contains("jpg")) return "jpg";
        if (contentType.contains("gif")) return "gif";
        if (contentType.contains("webp")) return "webp";
        return "png";
    }
}