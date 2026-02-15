package co.kr.user.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * JPA 엔티티의 필드 데이터를 데이터베이스에 저장할 때 암호화하고,
 * 데이터베이스에서 읽어올 때 복호화하는 변환기(Converter) 클래스들을 정의한 곳입니다.
 * 개인정보 보호를 위해 DB에는 암호화된 상태로 저장됩니다.
 */
public class CryptoConverter {

    /**
     * 결정적(Deterministic) 암호화를 수행하는 변환기입니다.
     * AES CBC 모드를 사용하여, 동일한 평문에 대해 항상 동일한 암호문을 생성합니다.
     * 주로 이메일이나 전화번호와 같이 '검색(Search)'이 필요한 컬럼에 사용됩니다.
     */
    @Converter
    @Component
    @RequiredArgsConstructor
    public static class DeterministicConverter implements AttributeConverter<String, String> {
        private final AESUtil aesUtil;

        /**
         * 엔티티의 데이터를 데이터베이스 컬럼 데이터로 변환합니다. (암호화)
         * @param attribute 엔티티의 필드 값 (평문)
         * @return DB에 저장될 값 (암호문)
         */
        @Override
        public String convertToDatabaseColumn(String attribute) {
            // 값이 존재할 경우에만 AES CBC 모드로 암호화 수행
            return StringUtils.hasText(attribute) ? aesUtil.CBCencrypt(attribute) : attribute;
        }

        /**
         * 데이터베이스의 컬럼 데이터를 엔티티의 데이터로 변환합니다. (복호화)
         * @param dbData DB에 저장된 값 (암호문)
         * @return 엔티티의 필드 값 (평문)
         */
        @Override
        public String convertToEntityAttribute(String dbData) {
            // 값이 존재할 경우에만 AES CBC 모드로 복호화 수행
            return StringUtils.hasText(dbData) ? aesUtil.CBCdecrypt(dbData) : dbData;
        }
    }

    /**
     * GCM(Galois/Counter Mode) 암호화를 수행하는 변환기입니다.
     * 암호화할 때마다 랜덤한 IV(Initial Vector)를 사용하므로, 동일한 평문이라도 매번 다른 암호문이 생성됩니다.
     * 보안성이 더 높으나, 암호문이 매번 달라지므로 DB 상에서 직접적인 검색(Equality Check)은 불가능합니다.
     * 이름, 주소 등 검색보다는 '저장 및 조회'가 주목적인 민감 정보에 사용됩니다.
     */
    @Converter
    @Component
    @RequiredArgsConstructor
    public static class GcmConverter implements AttributeConverter<String, String> {
        private final AESUtil aesUtil;

        /**
         * 엔티티의 데이터를 데이터베이스 컬럼 데이터로 변환합니다. (암호화)
         * @param attribute 엔티티의 필드 값 (평문)
         * @return DB에 저장될 값 (암호문)
         */
        @Override
        public String convertToDatabaseColumn(String attribute) {
            // 값이 존재할 경우에만 AES GCM 모드로 암호화 수행
            return StringUtils.hasText(attribute) ? aesUtil.GCMencrypt(attribute) : attribute;
        }

        /**
         * 데이터베이스의 컬럼 데이터를 엔티티의 데이터로 변환합니다. (복호화)
         * @param dbData DB에 저장된 값 (암호문)
         * @return 엔티티의 필드 값 (평문)
         */
        @Override
        public String convertToEntityAttribute(String dbData) {
            // 값이 존재할 경우에만 AES GCM 모드로 복호화 수행
            return StringUtils.hasText(dbData) ? aesUtil.GCMdecrypt(dbData) : dbData;
        }
    }
}