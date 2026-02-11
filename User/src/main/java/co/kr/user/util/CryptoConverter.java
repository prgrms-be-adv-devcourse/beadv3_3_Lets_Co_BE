package co.kr.user.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Converter
@Component
@RequiredArgsConstructor
public class CryptoConverter implements AttributeConverter<String, String> {

    private final AESUtil aesUtil;

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (!StringUtils.hasText(attribute)) {
            return attribute;
        }
        return aesUtil.encrypt(attribute);
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (!StringUtils.hasText(dbData)) {
            return dbData;
        }
        return aesUtil.decrypt(dbData);
    }
}