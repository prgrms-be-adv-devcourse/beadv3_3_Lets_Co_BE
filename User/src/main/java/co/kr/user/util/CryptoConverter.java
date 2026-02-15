package co.kr.user.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

public class CryptoConverter {

    @Converter
    @Component
    @RequiredArgsConstructor
    public static class DeterministicConverter implements AttributeConverter<String, String> {
        private final AESUtil aesUtil;

        @Override
        public String convertToDatabaseColumn(String attribute) {
            return StringUtils.hasText(attribute) ? aesUtil.CBCencrypt(attribute) : attribute;
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            return StringUtils.hasText(dbData) ? aesUtil.CBCdecrypt(dbData) : dbData;
        }
    }

    @Converter
    @Component
    @RequiredArgsConstructor
    public static class GcmConverter implements AttributeConverter<String, String> {
        private final AESUtil aesUtil;

        @Override
        public String convertToDatabaseColumn(String attribute) {
            return StringUtils.hasText(attribute) ? aesUtil.GCMencrypt(attribute) : attribute;
        }

        @Override
        public String convertToEntityAttribute(String dbData) {
            return StringUtils.hasText(dbData) ? aesUtil.GCMdecrypt(dbData) : dbData;
        }
    }
}