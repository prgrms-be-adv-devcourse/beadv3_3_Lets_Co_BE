package co.kr.user.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 엔티티 필드를 DB 저장 시 자동 암호화, 조회 시 자동 복호화해주는 컨버터입니다.
 */
@Converter
@Component
@RequiredArgsConstructor
public class CryptoConverter implements AttributeConverter<String, String> {

    private final AESUtil aesUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        // 데이터가 null이거나 빈 문자열이면 변환하지 않음
        if (attribute == null || attribute.isBlank()) {
            return attribute;
        }
        return aesUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        // DB 데이터가 null이거나 빈 문자열이면 변환하지 않음
        if (dbData == null || dbData.isBlank()) {
            return dbData;
        }
        return aesUtil.decrypt(dbData);
    }
}