package co.kr.user.util;

import co.kr.user.model.vo.UserDel;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA 엔티티의 UserDel Enum 타입과 데이터베이스의 Integer 타입 간의 변환을 담당하는 컨버터 클래스입니다.
 * DB에는 정수값(0, 1, 2)으로 저장하고, 코드에서는 의미 있는 Enum(ACTIVE, DELETED, PENDING)으로 사용하기 위함입니다.
 */
@Converter(autoApply = true) // autoApply = true: UserDel 타입이 사용되는 모든 엔티티 필드에 이 컨버터를 자동으로 적용합니다.
public class UserDelConverter implements AttributeConverter<UserDel, Integer> {

    /**
     * 엔티티의 UserDel 값을 데이터베이스에 저장할 Integer 값으로 변환합니다.
     * @param attribute 엔티티의 UserDel Enum 값
     * @return DB 컬럼에 저장될 정수 값 (null이면 null 반환)
     */
    @Override
    public Integer convertToDatabaseColumn(UserDel attribute) {
        // Enum 객체가 null이 아니면 해당 Enum의 정수 값(value)을 반환
        return (attribute == null) ? null : attribute.getValue();
    }

    /**
     * 데이터베이스에서 조회한 Integer 값을 엔티티의 UserDel Enum으로 변환합니다.
     * @param dbData DB 컬럼에 저장된 정수 값
     * @return 엔티티 필드에 매핑될 UserDel Enum 값
     */
    @Override
    public UserDel convertToEntityAttribute(Integer dbData) {
        // DB 값이 null이 아니면 해당 정수 값에 대응하는 UserDel Enum을 찾아 반환
        return (dbData == null) ? null : UserDel.fromValue(dbData);
    }
}